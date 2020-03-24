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
import models.DatabaseScope
import models.error.ApiError
import models.templates.{Template, TemplateType}
import monix.eval.Task
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

@ImplementedBy(classOf[IndexerImpl])
trait Indexer[Content, TransformedContent, Effect[_], UploadResult, QueryResult] {

  def transform(jsonContent: Content, template: Template): TransformedContent
  def load(jsValue: TransformedContent): Effect[UploadResult]
  def queryByType(templateType: TemplateType, databaseScope: DatabaseScope, token: String): Effect[QueryResult]

  def queryByTypeAndId(
    templateType: TemplateType,
    id: UUID,
    databaseScope: DatabaseScope,
    token: String
  ): Effect[QueryResult]

}

class IndexerImpl @Inject()(
  WSClient: WSClient,
  templateEngine: TemplateEngine[JsValue, JsValue],
  configuration: Configuration
) extends Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]] {

  val queryEndpoint = configuration.get[String]("kgquery.endpoint")

  override def load(jsValue: JsValue): Task[WSResponse] = ???

  override def transform(jsonContent: JsValue, template: Template): JsValue = {
    templateEngine.transform(jsonContent, template)
  }

  override def queryByType(
    templateType: TemplateType,
    databaseScope: DatabaseScope,
    token: String
  ): Task[Either[ApiError, JsValue]] = ???

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
}
