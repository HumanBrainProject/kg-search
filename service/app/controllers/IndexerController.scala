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
package controllers

import java.util.UUID

import akka.util.ByteString
import javax.inject.Inject
import models.DatabaseScope
import models.error.ApiError
import models.templates.TemplateType
import monix.eval.Task
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, ResponseHeader, Result}
import services.indexer.Indexer

import scala.concurrent.Future

class IndexerController @Inject()(
  indexer: Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]],
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val s = monix.execution.Scheduler.Implicits.global
  def indexByType(databaseScope: DatabaseScope, templateType: TemplateType) = ???

  def indexByTypeAndId(databaseScope: DatabaseScope, templateType: TemplateType, id: UUID): Action[AnyContent] = ???

  def applyTemplateByTypeAndId(
    databaseScope: DatabaseScope,
    templateType: TemplateType,
    id: UUID
  ): Action[AnyContent] =
    Action.async { implicit request =>
      val result = request.headers.toSimpleMap.get("Authorization") match {
        case Some(token) =>
          indexer
            .queryByTypeAndId(templateType, id, databaseScope, token)
            .map {
              case Right(v) => Ok(v)
              case Left(error) =>
                Result(ResponseHeader(error.status), HttpEntity.Strict(ByteString(error.message), None))
            }

        case None => Task.pure(Unauthorized("Please provide credentials"))
      }
      result.runToFuture(s)
    }

}
