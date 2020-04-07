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
import javax.inject.Inject
import models.DatabaseScope
import models.errors.ApiError
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.concurrent.ExecutionContext
import scala.util.Success

@ImplementedBy(classOf[ElasticSearchImpl])
trait ElasticSearch {

  def updateLabels(labels: Map[String, JsValue]): Unit

  def recreateIndex(mapping: Map[String, Map[String, JsValue]], indexName: DatabaseScope): Task[Either[ApiError, Unit]]

  def index(
    jsonPayload: JsObject,
    dataType: String,
    identifier: String,
    indexName: DatabaseScope
  ): Task[Either[ApiError, Unit]]

  def getNotUpdatedInstances(
    indexName: String,
    indexTime: String,
    page: Int = 0,
    size: Int = 50
  ): Task[Either[ApiError, List[String]]]

  def removeIndex(id: String, indexName: String): Task[Either[ApiError, Unit]]
}

class ElasticSearchImpl @Inject()(
  WSClient: WSClient,
  configuration: Configuration
) extends ElasticSearch {
  val elasticSearchEndpoint = configuration.get[String]("es.host")
  override def updateLabels(labels: Map[String, JsValue]): Unit = {}

  override def recreateIndex(
    mapping: Map[String, Map[String, JsValue]],
    dbScope: DatabaseScope
  ): Task[Either[ApiError, Unit]] = {
    Task
      .fromFuture(WSClient.url(s"$elasticSearchEndpoint/${dbScope.toIndexName}").delete())
      .flatMap(
        res =>
          res.status match {
            case OK =>
              //recreate
              Task
                .fromFuture(
                  WSClient
                    .url(s"$elasticSearchEndpoint/${dbScope.toIndexName}")
                    .put(Json.toJson(mapping)(Writes.genericMapWrites))
                )
                .map(
                  res =>
                    res.status match {
                      case CREATED => Right(())
                      case s       => Left(ApiError(s, res.body))
                  }
                )
            case s => Task.pure(Left(ApiError(s, res.body)))
        }
      )
  }

  override def index(
    jsonPayload: JsObject,
    dataType: String,
    identifier: String,
    databaseScope: DatabaseScope
  ): Task[Either[ApiError, Unit]] = {
    // TODO Check if element is a list ????
    Task
      .fromFuture(
        WSClient.url(s"$elasticSearchEndpoint/${databaseScope.toIndexName}/$dataType/$identifier").put(jsonPayload)
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
    Task.fromFuture(WSClient.url(s"$elasticSearchEndpoint/$indexName/_search").post(body)).map { res =>
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
    Task.from(WSClient.url(s"$elasticSearchEndpoint/$indexName/$id").delete()).map { res =>
      res.status match {
        case OK => Right(())
        case e  => Left(ApiError(e, res.body))
      }
    }
  }
}

object ElasticSearchImpl {}
