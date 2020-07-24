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
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue, Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.collection.immutable.HashMap
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.Success

@ImplementedBy(classOf[ElasticSearchImpl])
trait ElasticSearch {

  def updateLabels(labels: Map[String, JsValue]): Unit

  def recreateIndex(
    mapping: JsValue,
    relevantType: TemplateType,
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
    indexname: String = ESHelper.publicIndexPrefix
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
    mapping: JsValue,
    relevantType: TemplateType,
    dbScope: DatabaseScope,
    completeRebuild: Boolean,
  ): Task[Either[ApiError, Unit]] = {
    if (completeRebuild) {
      logger.info(s"Completely rebuilding the index ${dbScope.toIndexName}_${relevantType.apiName.toLowerCase} in ES...")
      Task
        .deferFuture(WSClient.url(s"$elasticSearchEndpoint/${dbScope.toIndexName}_${relevantType.apiName.toLowerCase}").delete())
        .flatMap(
          res =>
            res.status match {
              case OK | NOT_FOUND =>
                Task
                  .deferFuture(
                    WSClient
                      .url(s"$elasticSearchEndpoint/${dbScope.toIndexName}_${relevantType.apiName.toLowerCase}").put(mapping)
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
    logger.info(s"Started the ingestion of the data for ${databaseScope.toIndexName}_${dataType.toLowerCase}/_doc/$identifier")
    Task
      .deferFuture(
        WSClient.url(s"$elasticSearchEndpoint/${databaseScope.toIndexName}_${dataType.toLowerCase}/_doc/$identifier").put(jsonPayload)
      )
      .map { res =>
        res.status match {
          case CREATED | OK => Right(())
          case e => Left(ApiError(e, res.body))
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
          val listOfIds: List[String] = (res.json \ "hits" \ "hits").asOpt[List[JsObject]].getOrElse(List()).map(el => s"${(el \ "_id").as[String]}")
          logger.debug(s"Elasticsearch returned ${listOfIds.size} instances which haven't been updated recently")
          Right(listOfIds)
        case s => Left(ApiError(s, res.body))
      }
    }
  }

  override def removeIndex(id: String, indexName: String): Task[Either[ApiError, Unit]] = {
    Task.deferFuture(WSClient.url(s"$elasticSearchEndpoint/$indexName/_doc/$id").delete()).map { res =>
      res.status match {
        case OK => {
          logger.debug(s"Successfully removed instance $indexName/_doc/$id")
          Right(())
        }
        case e =>
          logger.error(s"Was not able to remove instance $indexName/_doc/$id - Error(${res.status} - ${res.body})")
          Left(ApiError(e, res.body))
      }
    }
  }

  private def doQueryIndexByType(
                                  templateType: TemplateType,
                                  indexname: String = ESHelper.publicIndexPrefix,
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
              s"$elasticSearchEndpoint/${indexname}_${templateType.apiName.toLowerCase}/_search?size=$size&from=${currentPage * size}"
            ).post(Json.obj("_source" -> JsString("identifier.value")))
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
    indexname: String = ESHelper.publicIndexPrefix
  ): Task[Either[ApiError, List[JsObject]]] = {
    val querySize = 1000
    Task
      .deferFuture(
        WSClient.url(s"$elasticSearchEndpoint/${indexname}_${templateType.apiName.toLowerCase}/_search?size=$querySize&from=0").post(Json.obj("_source" -> JsString("identifier.value")))
      )
      .flatMap { res =>
        res.status match {
          case OK =>
            val hits = (res.json \ "hits" \ "hits").asOpt[List[JsObject]]
            val total = (res.json \ "hits" \ "total" \ "value").asOpt[Long]
            (total, hits) match {
              case (_, Some(Nil)) | (_, None) => Task.pure(Right(List()))
              case (Some(t), Some(l)) if l.length < t =>
                val startingPage = 1
                val overFlow = if (t % querySize > 0) 1 else 0
                val totalPages = (t / querySize) + overFlow
                doQueryIndexByType(templateType, indexname, querySize, startingPage, totalPages).map {
                  case Right(value) => Right(l ++ value)
                  case Left(e)      => Left(ApiError(INTERNAL_SERVER_ERROR, s"Could not paginate data from es index - ${e.status}"))
                }
              case (Some(t), Some(l)) if l.length >= t => Task.pure(Right(l))
            }
          case _ =>
            Task.pure(Left(ApiError(INTERNAL_SERVER_ERROR, s"Could not fetch data from es index - ${res.status}")))
        }
      }
  }
}

object ElasticSearchImpl {}
