
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

package controllers

import actions.EditorUserAction
import com.google.inject.Inject
import constants.EditorConstants
import helpers.EditorResponseHelper
import models.editorUserList.BOOKMARKFOLDER
import play.api.http.HeaderNames._
import models.instance.{NexusInstance, NexusInstanceReference}
import models.{AuthenticatedUserAction, EditorResponseObject, NexusPath, UserRequest}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{AnyContent, _}
import services._
import services.bookmark.EditorBookmarkService

import scala.concurrent.{ExecutionContext, Future}

class NexusEditorUserController @Inject()(
                                           cc: ControllerComponents,
                                           config: ConfigurationService,
                                           authenticatedUserAction: AuthenticatedUserAction,
                                           editorUserService: EditorUserService,
                                           editorUserListService: EditorBookmarkService,
                                           arangoQueryService: ArangoQueryService,
                                           nexusService: NexusService,
                                           oIDCAuthService: OIDCAuthService,
                                           formService:FormService
                                         )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  val logger = Logger(this.getClass)

  private def getOrCreateUserWithUserFolder( token: String)(implicit request: UserRequest[AnyContent] ) = {
    editorUserService.getUser(request.user, token).flatMap{
      case Right(editorUser) => Future(Some(editorUser))
      case Left(_) =>
        editorUserService.createUser(request.user, token).flatMap{
          case Right(editorUser) =>
            editorUserListService.createBookmarkListFolder(editorUser, "My Bookmarks",token, BOOKMARKFOLDER).map{
              case Right(_) =>
               Some(editorUser)
              case Left(_) =>
                logger.info(s"Deleting editor user with id : ${request.user.id}")
                nexusService.deprecateInstance(config.nexusEndpoint, EditorConstants.editorUserPath,
                  editorUser.nexusId.id, 1L, token
                )
                None
            }
          case Left(_) => Future(None)
        }
    }
  }

  def getOrCreateCurrentUser(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
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

  def getBookmarkListFolders():Action[AnyContent] = (authenticatedUserAction andThen
    EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
    for{
      token <- oIDCAuthService.getTechAccessToken()
      res <-  editorUserListService.getUserBookmarkLists(request.editorUser, formService.formRegistry, token).map {
          case Left(r) => r.toResult
          case Right(l) => Ok(Json.toJson(EditorResponseObject(Json.toJson(l))))
        }
    } yield res
  }


  def getInstancesOfBookmarkListBySchema(
                     org: String,
                     domain: String,
                     datatype: String,
                     version: String,
                     from: Option[Int],
                     size: Option[Int],
                     search: String
                   ): Action[AnyContent] = authenticatedUserAction.async  { implicit request =>
    val nexusPath = NexusPath(org, domain, datatype, version)
    val token = request.headers.toSimpleMap.getOrElse(AUTHORIZATION, "")
    arangoQueryService.listInstances(nexusPath, from, size, search, token).map{
      case Right(data) => Ok(Json.toJson(data))
      case Left(res) => res.toResult
    }
  }

  def getInstancesbyBookmarkList(
                               org: String,
                               domain: String,
                               datatype: String,
                               version: String,
                               id: String,
                               from: Int,
                               size: Int,
                               search: String
                             ): Action[AnyContent] = (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
    val nexusRef = NexusInstanceReference(org, domain, datatype, version, id)
    val token = request.headers.toSimpleMap.getOrElse(AUTHORIZATION, "")
    editorUserListService.getInstancesOfBookmarkList(nexusRef, from, size, search, token).map{
      case Right( (instances, total) ) => Ok(Json.toJson(EditorResponseObject(Json.toJson(instances))).as[JsObject].+("total" -> JsNumber(total)) )
      case Left(error) => error.toResult
    }
  }

  def createBookmarkList : Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
      val opts = for {
        json <- request.body.asJson
        name <- (json \ "name").asOpt[String]
        folderId <- (json \ "folderId").asOpt[String]
      } yield (name, folderId)
      opts match {
        case Some ((n, id))  =>
          for {
            token <- oIDCAuthService.getTechAccessToken()
            result <- editorUserListService.createBookmarkList(n, id, token).map{
              case Left(r) => r.toResult
              case Right(bookmarkList) => Created(Json.toJson(bookmarkList))
            }
          } yield result

        case _ => Future(BadRequest("Missing parameters"))
      }
  }

  def updateBookmarkList(
                          org: String,
                          domain: String,
                          datatype: String,
                          version: String,
                          id: String,
                        ):Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
      val opts = for {
        json <- request.body.asJson
        name <- (json \ "name").asOpt[String]
      } yield name
      opts match {
        case Some (newName)  =>
          val ref = NexusInstanceReference(org, domain, datatype, version, id)
          for {
            token <- oIDCAuthService.getTechAccessToken()
            result <- editorUserListService.getBookmarkListById(ref, token).flatMap[Result]{
              case Left(error) => Future(error.toResult)
              case Right((bookmarkList, userFolderId) ) =>
                val updatedBookmarkList = bookmarkList.copy(name = newName)
                editorUserListService.updateBookmarkList(updatedBookmarkList, ref.nexusPath.withSpecificSubspace(config.editorSubSpace), id, userFolderId, token).map[Result]{
                  case Left(error) => error.toResult
                  case Right(_) => NoContent
                }
            }
      } yield result
        case _ => Future(BadRequest("Missing parameters"))
      }
    }

  def deleteBookmarkList(org: String, domain:String, schema: String, version:String, id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
      val bookmarkRef = NexusInstanceReference(org, domain, schema, version, id)
      for{
        token <- oIDCAuthService.getTechAccessToken()
        result <- editorUserListService.deleteBookmarkList(bookmarkRef, token).map {
          case Left(error) => InternalServerError(Json.toJson(error))
          case Right(()) => NoContent
        }
      } yield result
    }

  def updateBookmarks(org: String, domain: String, schema: String, version: String, id: String): Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
      val bookmarkIds = for {
        json <- request.body.asJson
        arrayOfIds <- json.asOpt[List[String]]
      } yield arrayOfIds.map(NexusInstanceReference.fromUrl)
      val instanceReference = NexusInstanceReference(org, domain, schema, version, id)
      bookmarkIds match {
        case Some(ids) =>
          val futList = for {
            token <- oIDCAuthService.getTechAccessToken()
            listResult <- editorUserListService.updateBookmarks(instanceReference, ids, request.editorUser, token)
          } yield listResult

          futList.map { listResponse =>
            if (listResponse.forall(_.isRight)) {
              Created("Bookmarks updated")
            } else {
              val errors = listResponse.filter(_.isLeft).mkString("\n")
              logger.error(s"Error while updating bookmark -${errors}")
              InternalServerError(s"Could not update all the bookmarks - $errors")
            }
          }

        case None => Future(BadRequest("Missing body content"))
      }
    }

  def retrieveBookmarks : Action[AnyContent] =
    (authenticatedUserAction andThen EditorUserAction.editorUserAction(editorUserService)).async { implicit request =>
      import EditorBookmarkService.JsEither._
      val instanceList = for{
        json <- request.body.asJson
        instances <- json.asOpt[List[String]]
      } yield  instances.map(l => NexusInstanceReference.fromUrl(l))
      instanceList match {
        case Some(l) =>
          oIDCAuthService.getTechAccessToken().flatMap{
            token =>
              editorUserListService.retrieveBookmarkLists(l, request.editorUser, token).map{
                res =>
                  val json = res.map(el => Json.obj("id" -> el._1.toString, "bookmarkLists" -> el._2))
                  Ok(Json.toJson(EditorResponseObject(Json.toJson(json))))
              }
          }
        case None => Future(BadRequest("Missing body content"))
      }
    }
}