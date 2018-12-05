
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

package actions

import helpers.EditorSpaceHelper
import models.user.{EditorUserRequest, EditorUserWriteRequest}
import models.{IAMPermission, UserRequest, user}
import play.api.Logger
import play.api.mvc.Results._
import play.api.http.HeaderNames._
import play.api.mvc._
import services.{EditorUserService, IAMAuthService}

import scala.concurrent.{ExecutionContext, Future}


object EditorUserAction{
  val logger = Logger(this.getClass)
  def editorUserWriteAction(org: String, editorSuffix: String, iAMAuthService: IAMAuthService)
                           (implicit ec: ExecutionContext): ActionRefiner[UserRequest, EditorUserWriteRequest] =
    new ActionRefiner[UserRequest, EditorUserWriteRequest] {
    def executionContext: ExecutionContext = ec
    def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserWriteRequest[A]]] = {
      val editorOrg = if(org.endsWith(editorSuffix)) org else org + editorSuffix
      if(EditorSpaceHelper.isEditorGroup(input.user, editorOrg) ){
        val token = input.request.headers.toSimpleMap.getOrElse("Authorization", "")
        iAMAuthService.getAcls(editorOrg, Seq(("self", "true"), ("parents", "true")), token).map {
          case Right(acls) =>
            if (IAMAuthService.hasAccess(acls, IAMPermission.Write)) {
              Right(EditorUserWriteRequest(input.user, org, input))
            } else {
              Left(Forbidden("You do not have sufficient access rights to proceed"))
            }
          case Left(response) =>
            logger.error(s"Fetching permission failed - ${response.body}")
            Left(InternalServerError("An error occurred while fetching permission"))
        }
      }else{
        Future.successful {
          logger.debug(s"Not allowed: ${input.user} for index: $org")
          Left(Forbidden("You are not allowed to perform this request"))
        }
      }
    }
  }

  def editorUserAction(editorUserService: EditorUserService)
                      (implicit ec: ExecutionContext): ActionRefiner[UserRequest, EditorUserRequest] =
    new ActionRefiner[UserRequest, EditorUserRequest] {
      def executionContext: ExecutionContext = ec
      def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserRequest[A]]] = {
        editorUserService.getUser(input.user, input.request.headers.toSimpleMap.getOrElse(AUTHORIZATION, "")).map{
          case Right(editorUser) => Right(user.EditorUserRequest(editorUser, input))
          case Left(err) => logger.error(s"Fetching editor user failed - ${err.content}")
            Left(NotFound("An error occurred while fetching user information"))
        }
      }
    }

}