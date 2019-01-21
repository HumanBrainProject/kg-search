/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.google.inject.Inject
import helpers.OIDCHelper
import models.AuthenticatedUserAction
import models.errors.APIEditorError
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.IDMAPIService

import scala.concurrent.ExecutionContext

class SuggestionController @Inject()(
  cc: ControllerComponents,
  IDMAPIService: IDMAPIService,
  authenticatedUserAction: AuthenticatedUserAction
)(
  implicit executionContext: ExecutionContext,
) extends AbstractController(cc) {

  def getUsers(size: Int, from: Int, search: String): Action[AnyContent] = authenticatedUserAction.async {
    implicit request =>
      val token = OIDCHelper.getTokenFromRequest(request)
      IDMAPIService.getUsers(size, from, search, token).map {
        case Right((users, pagination)) => Ok(Json.obj("users" -> users, "page" -> pagination))
        case Left(res)                  => APIEditorError(res.status, res.body).toResult
      }
  }

  def addUser(org: String, domain: String, schema: String, version: String, instanceId: String) = ???

  def acceptSuggestion(org: String, domain: String, schema: String, version: String, suggestionId: String) = ???

  def rejectSuggestion(org: String, domain: String, schema: String, version: String, suggestionId: String) = ???
}
