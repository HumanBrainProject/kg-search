
/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
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

package editor.controllers

import authentication.models.AuthenticatedUserAction
import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.FavoriteGroup
import editor.services.EditorUserService
import helpers.ResponseHelper
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class NexusEditorUserController @Inject()(
                                           cc: ControllerComponents,
                                           authenticatedUserAction: AuthenticatedUserAction,
                                           oIDCAuthService: OIDCAuthService,
                                           config: Configuration,
                                           editorUserService: EditorUserService,
                                         )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def createCurrentUser(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    editorUserService.createUser(request.user.id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => InternalServerError("An error occurred while creating the user")
    }
  }

  def getCurrentUser(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    editorUserService.getUser(request.user.id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => InternalServerError("An error occurred while retrieving the user")
    }
  }

  def addFavorite(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val instanceId = (request.body.asJson.get \ "instanceId").as[String]
    val favoriteGroupNexusId = (request.body.asJson.get \ "favoriteGroupNexusId").as[String]
    editorUserService.addFavorite(favoriteGroupNexusId, instanceId).map {
      case Some(favorite) => Created(Json.toJson(favorite))
      case None => InternalServerError("An error occured while creating a favorite")
    }
  }

  def deleteFavorite(org: String, domain:String, schema: String, version:String, id: String): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val instanceId = s"$org/$domain/$schema/$version/$id"
    editorUserService.removeFavorite(instanceId).map {
      res => ResponseHelper.forwardResultResponse(res)
    }
  }

  def createFavoriteGroup(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val name = (request.body.asJson.get \ "name").as[String]
    editorUserService.createFavoriteGroup(name, request.user.id).map {
      case Some(favoriteGroup) => Created(Json.toJson(favoriteGroup))
      case None => InternalServerError("An error occured while creating a favorite group")
    }
  }

}
