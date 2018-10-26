
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

import authentication.models.{AuthenticatedUserAction, UserRequest}
import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.{FavoriteGroup, NexusInstance, NexusPath, NexusUser}
import common.services.ConfigurationService
import editor.models.EditorUser
import editor.models.EditorUserList.BOOKMARK
import editor.services.{ArangoQueryService, EditorUserListService, EditorUserService}
import helpers.ResponseHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class NexusEditorUserController @Inject()(
                                           cc: ControllerComponents,
                                           config: ConfigurationService,
                                           authenticatedUserAction: AuthenticatedUserAction,
                                           editorUserService: EditorUserService,
                                           editorUserListService: EditorUserListService,
                                           arangoQueryService: ArangoQueryService,
                                           nexusService: NexusService,
                                           oIDCAuthService: OIDCAuthService
                                         )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  val logger = Logger(this.getClass)
  def createCurrentUser(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    for{
      token <- oIDCAuthService.getTechAccessToken()
      u <- getOrCreateUserWithUserFolder(token)
    } yield {
      u match {
        case Some(editorUser) => Ok(Json.toJson(editorUser))
        case None => InternalServerError("An error occurred while retrieving the user")
      }
    }
  }

  private def getOrCreateUserWithUserFolder( token: String)(implicit request: UserRequest[AnyContent] ) = {
    editorUserService.getUser(request.user.id).flatMap{
      case Some(editorUser) => Future(Some(editorUser))
      case None =>
        editorUserService.createUser(request.user.id, token).flatMap{
          case Some(editorUser) =>
            editorUserListService.createUserFolder(editorUser, "My Bookmarks", BOOKMARK, token).map{
              case Some(userFolder) =>
                Some(editorUser.copy(userFolders = List(userFolder)))
              case None =>
                logger.info(s"Deleting editor user with id : ${request.user.id}")
                nexusService.deprecateInstance(config.nexusEndpoint, EditorUserService.editorUserPath,
                  NexusInstance.extractIdAndPath(Json.obj("@id" -> editorUser.nexusId))._1, token
                )
                InternalServerError("An error occurred while creating the user")
                None
            }
          case None => Future(None)
        }
    }
  }

  def getCurrentUser(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    editorUserService.getUser(request.user.id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => InternalServerError("An error occurred while retrieving the user")
    }
  }

  private def getLists(nexusUser: NexusUser, editorUser: EditorUser): Future[Result] = {
    editorUserListService.getUserLists(nexusUser, editorUser).map {
      case Right(lists) => Ok(Json.toJson(lists))
      case Left(res) => InternalServerError(s"An error occurred while retrieving the user lists ${res.body}")
    }
  }

  def getUserLists():Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    for{
      token <- oIDCAuthService.getTechAccessToken()
      u <- getOrCreateUserWithUserFolder(token)
      res <- u.map( editorUser => getLists(request.user, editorUser))
        .getOrElse(Future(InternalServerError(s"An error occurred while retrieving the user lists")))
    } yield res
  }


  def getUserListBySchema(
                     org: String,
                     domain: String,
                     datatype: String,
                     version: String,
                     from: Option[Int],
                     size: Option[Int],
                     search: String
                   ): Action[AnyContent] = authenticatedUserAction.async  { implicit request =>
    val nexusPath = NexusPath(org, domain, datatype, version)
    arangoQueryService.listInstances(nexusPath, from, size, search).map{
      case Right(json) => Ok(json)
      case Left(res) => ResponseHelper.forwardResultResponse(res)
    }
  }

}
