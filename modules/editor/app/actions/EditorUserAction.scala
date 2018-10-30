
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

package editor.actions

import authentication.models.{IAMPermission, UserRequest}
import authentication.service.IAMAuthService
import play.api.mvc._
import play.api.mvc.Results._
import editor.helpers.EditorSpaceHelper
import editor.models.EditorUserRequest
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}


object EditorUserAction{
  val logger = Logger(this.getClass)
  def editorUserAction(org: String, editorSuffix: String, iAMAuthService: IAMAuthService)
                      (implicit ec: ExecutionContext): ActionRefiner[UserRequest, EditorUserRequest] =
    new ActionRefiner[UserRequest, EditorUserRequest] {
    def executionContext: ExecutionContext = ec
    def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserRequest[A]]] = {
      val editorOrg = if(org.endsWith(editorSuffix)) org else org + editorSuffix
      if(EditorSpaceHelper.isEditorGroup(input.user, editorOrg) ){
        iAMAuthService.getAcls(editorOrg, Seq(("self", "true"), ("parents", "true"))).map {
          case Right(acls) =>
            if (IAMAuthService.hasAccess(acls, IAMPermission.Write)) {
              Right(EditorUserRequest(input.user, org, input))
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

}