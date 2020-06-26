/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package services.indexer

import com.google.inject.ImplementedBy
import helpers.ESHelper
import javax.inject.Inject
import models.DatabaseScope
import models.errors.ApiError
import models.templates.TemplateType
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.{Configuration, Logging}
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.Success

@ImplementedBy(classOf[ElasticSearchImpl])
trait ElasticSearch {

  def updateLabels(labels: Map[String, JsValue]): Unit

  def recreateIndex(
    mapping: Map[String, Map[String, JsValue]],
    indexName: DatabaseScope,
    completeRebuild: Boolean,
  ): Task[Either[ApiError, Unit]]

  def index(
    jsonPayload: JsObject,
    dataType: String,
    identifier: String,
    indexName: DatabaseScope,
  ): Task[Either[ApiError, Unit]]

  def getNotUpdatedInstances(
    indexName: String,
    indexTime: String,
    page: Int = 0,
    size: Int = 50
  ): Task[Either[ApiError, List[String]]]

  def removeIndex(id: String, indexName: String): Task[Either[ApiError, Unit]]

  def queryIndexByType(
    templateType: TemplateType,
    indexname: String = ESHelper.publicIndex
  ): Task[Either[ApiError, List[JsObject]]]
}

class ElasticSearchImpl @Inject()(
  WSClient: WSClient,
  configuration: Configuration
) extends ElasticSearch
    with Logging {
  val elasticSearchEndpoint: String = configuration.get[String]("es.host")
  override def updateLabels(labels: Map[String, JsValue]): Unit = {}

  override def recreateIndex(
    mapping: Map[String, Map[String, JsValue]],
    dbScope: DatabaseScope,
    completeRebuild: Boolean,
  ): Task[Either[ApiError, Unit]] = {
    if (completeRebuild) {
      logger.info(s"Completely rebuilding the index ${dbScope.toIndexName} in ES...")
      Task
        .deferFuture(WSClient.url(s"$elasticSearchEndpoint/${dbScope.toIndexName}").delete())
        .flatMap(
          res =>
            res.status match {
              case OK | NOT_FOUND =>
                //recreate
                val payload = Json.toJson(mapping)(Writes.genericMapWrites)
                Task
                  .deferFuture(
                    WSClient
                      .url(s"$elasticSearchEndpoint/${dbScope.toIndexName}").withRequestTimeout(3600.seconds)
                      .put(payload)
                  )
                  .map(
                    res =>
                      res.status match {
                        case OK | CREATED =>
                          logger.debug("Success - Creating ES Mapping")
                          Right(())
                        case s =>
                          logger.error(s"Error - Creating ES Mapping - ${s} : ${res.body}")
                          Left(ApiError(s, res.body))
                    }
                  )
              case s =>
                logger.error(s"Error - Creating ES Mapping - ${s} : ${res.body}")
                Task.pure(Left(ApiError(s, res.body)))
          }
        )
    } else {
      Task.pure(Right(()))
    }
  }

  override def index(
    jsonPayload: JsObject,
    dataType: String,
    identifier: String,
    databaseScope: DatabaseScope,
  ): Task[Either[ApiError, Unit]] = {
    // TODO Check if element is a list ????
    Task
      .deferFuture(
        WSClient.url(s"$elasticSearchEndpoint/${databaseScope.toIndexName}/$dataType/$identifier").withRequestTimeout(60.minutes).put(jsonPayload)
      )
      .map { res =>
        res.status match {
          case CREATED | OK => Right(())
          case e            => Left(ApiError(e, res.body))
        }
      }

  }

  override def getNotUpdatedInstances(
    indexName: String,
    indexTime: String,
    page: Int = 0,
    size: Int = 50
  ): Task[Either[ApiError, List[String]]] = {
    val body = Json.obj(
      "query" -> Json.obj(
        "range" -> Json.obj(
          "@timestamp" -> Json.obj("lt" -> indexTime)
        )
      ),
      "from" -> JsNumber(page),
      "size" -> JsNumber(size)
    )
    Task.deferFuture(WSClient.url(s"$elasticSearchEndpoint/$indexName/_search").post(body)).map { res =>
      res.status match {
        case OK | CREATED =>
          val listOfIds: List[String] = (res.json \ "hits" \ "hits").asOpt[List[JsObject]].getOrElse(List()).map { el =>
            val t = (el \ "_type").as[String]
            val id = (el \ "_id").as[String]
            s"$t/$id"
          }
          Right(listOfIds)
        case s => Left(ApiError(s, res.body))
      }
    }
  }

  override def removeIndex(id: String, indexName: String): Task[Either[ApiError, Unit]] = {
    Task.deferFuture(WSClient.url(s"$elasticSearchEndpoint/$indexName/$id").delete()).map { res =>
      res.status match {
        case OK => Right(())
        case e  => Left(ApiError(e, res.body))
      }
    }
  }

  private def doQueryIndexByType(
    templateType: TemplateType,
    indexname: String = ESHelper.publicIndex,
    size: Long,
    startingPage: Long,
    totalPages: Long
  ): Task[Either[ApiError, List[JsObject]]] = {
    val listOfTask = for {
      currentPage <- startingPage to totalPages
    } yield {
      Task
        .deferFuture(
          WSClient
            .url(
              s"$elasticSearchEndpoint/$indexname/${templateType.apiName}/_search?size=$size&from=${currentPage * size}"
            )
            .get()
        )
    }
    Task
      .sequence(listOfTask)
      .map { l =>
        val errors = l.filter(r => r.status != OK)
        if (errors.nonEmpty) {
          Left(ApiError(errors.head.status, errors.map(_.body).mkString("\n")))
        } else {
          val hits = l.map { r =>
            (r.json \ "hits" \ "hits").asOpt[List[JsObject]]
          }
          if (hits.exists(s => s.isEmpty)) {
            Left(ApiError(INTERNAL_SERVER_ERROR, "Could not parse data from ES response"))
          } else {
            Right(hits.collect { case Some(v) => v }.flatten.toList)
          }
        }
      }
  }

  override def queryIndexByType(
    templateType: TemplateType,
    indexname: String = ESHelper.publicIndex
  ): Task[Either[ApiError, List[JsObject]]] = {
    val querySize = 1000
    Task
      .deferFuture(
        WSClient.url(s"$elasticSearchEndpoint/$indexname/${templateType.apiName}/_search?size=$querySize&from=0").get()
      )
      .flatMap { res =>
        res.status match {
          case OK =>
            val hits = (res.json \ "hits" \ "hits").asOpt[List[JsObject]]
            val total = (res.json \ "hits" \ "total").asOpt[Long]
            (total, hits) match {
              case (_, Some(Nil)) | (_, None) => Task.pure(Right(List()))
              case (Some(t), Some(l)) if l.length == t =>
                val startingPage = 1
                val overFlow = if (t % querySize > 0) 1 else 0
                val totalPages = (t / querySize) + overFlow
                doQueryIndexByType(templateType, indexname, querySize, startingPage, totalPages).map {
                  case Right(value) => Right(l ++ value)
                  case Left(e)      => Left(e)
                }
              case (Some(t), Some(l)) if l.length < t => Task.pure(Right(l))
              case _ =>
                Task.pure(Left(ApiError(INTERNAL_SERVER_ERROR, s"Could not fetch data from es index - ${res.status}")))
            }
        }
      }
  }
}

object ElasticSearchImpl {}
