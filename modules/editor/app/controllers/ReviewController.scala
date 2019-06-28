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

import com.google.inject.Inject
import models.errors.APIEditorError
import models.{AuthenticatedUserAction, EditorResponseObject}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services._
import services.instance.InstanceApiService
import services.specification.FormService

class ReviewController @Inject()(
  cc: ControllerComponents,
  IDMAPIService: IDMAPIService,
  authenticatedUserAction: AuthenticatedUserAction,
  formService: FormService,
  editorUserService: EditorUserService,
  editorService: EditorService,
  reverseLinkService: ReverseLinkService
) extends AbstractController(cc) {
  implicit val scheduler = monix.execution.Scheduler.Implicits.global
  object instanceApiService extends InstanceApiService

  def getUsers(size: Int, search: String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    IDMAPIService
      .getUsers(size, search, request.userToken)
      .map {
        case Right((users, pagination)) =>
          Ok(
            Json
              .toJson(EditorResponseObject(Json.obj("users" -> users)))
              .as[JsObject] ++ Json.toJson(pagination).as[JsObject]
          )
        case Left(res) => APIEditorError(res.status, res.body).toResult
      }
      .runToFuture
  }

  def getUserById(id: String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    IDMAPIService
      .getUserInfoFromID(id, request.userToken)
      .map {
        case Some(user) => Ok(Json.toJson(EditorResponseObject(Json.toJson(user))))
        case None       => NotFound("User not found")
      }
      .runToFuture
  }
}
