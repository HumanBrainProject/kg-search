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

import java.util.UUID

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{DatabaseScope, PaginationParams}
import models.errors.ApiError
import models.templates.{Template, TemplateType}
import monix.eval.Task
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

@ImplementedBy(classOf[IndexerImpl])
trait Indexer[Content, TransformedContent, Effect[_], UploadResult, QueryResult] {

  def transform(jsonContent: Content, template: Template): TransformedContent
  def transformMeta(jsonContent: Content, template: Template): TransformedContent
  def load(jsValue: TransformedContent): Effect[UploadResult]

  def queryByType(
    templateType: TemplateType,
    databaseScope: DatabaseScope,
    paginationParams: PaginationParams,
    token: String
  ): Effect[QueryResult]

  def queryByTypeAndId(
    templateType: TemplateType,
    id: UUID,
    databaseScope: DatabaseScope,
    token: String
  ): Effect[QueryResult]

  def metaByType(
    templateType: TemplateType,
    token: String
  ): Effect[QueryResult]

  def getLabels(
    templateType: TemplateType,
    token: String
  ): Effect[QueryResult]

}

class IndexerImpl @Inject()(
  WSClient: WSClient,
  templateEngine: TemplateEngine[JsValue, JsValue],
  configuration: Configuration
) extends Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]] {

  val queryEndpoint: String = configuration.get[String]("kgquery.endpoint")

  override def load(jsValue: JsValue): Task[WSResponse] = ???
  override def transform(jsonContent: JsValue, template: Template): JsValue = {
    templateEngine.transform(jsonContent, template)
  }
  override def transformMeta(jsonContent: JsValue, template: Template): JsValue = {
    templateEngine.transformMeta(jsonContent, template)
  }

  override def queryByType(
    templateType: TemplateType,
    databaseScope: DatabaseScope,
    paginationParams: PaginationParams,
    token: String
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(
            s"$queryEndpoint/query/$schema/search/instances/?vocab=https://schema.hbp.eu/search/&size=${paginationParams.limit}&from=${paginationParams.offset}"
          )
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val maybeListOfResults = for {
              jsonResponse <- wsresult.json.asOpt[Map[String, JsValue]]
              results      <- jsonResponse.get("results")
              resultArray  <- results.asOpt[List[JsObject]]
            } yield resultArray

            maybeListOfResults match {
              case Some(listOfResults) =>
                val template = templateEngine.getTemplateFromType(templateType, databaseScope)
                val result = listOfResults.map(r => {
                  transform(r, template)
                })
                Right(Json.toJson(result))
              case None => Left(ApiError(INTERNAL_SERVER_ERROR, "Could not unpack json result"))
            }
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def queryByTypeAndId(
    templateType: TemplateType,
    id: UUID,
    databaseScope: DatabaseScope,
    token: String
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search/instances/${id.toString}?vocab=https://schema.hbp.eu/search/")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val template = templateEngine.getTemplateFromType(templateType, databaseScope)
            Right(transform(wsresult.json, template))
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def metaByType(templateType: TemplateType, token: String): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val metaTemplate = templateEngine.getMetaTemplateFromType(templateType)
            Right(transformMeta(wsresult.json, metaTemplate))
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }
  override def getLabels(templateType: TemplateType, token: String): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val template = templateEngine.getESTemplateFromType(templateType)
            Right(transformMeta(wsresult.json, template))
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }
}
