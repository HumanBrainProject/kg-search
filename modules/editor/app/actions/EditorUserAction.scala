
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

import authentication.models.UserRequest
import play.api.mvc._
import play.api.mvc.Results._
import com.google.inject.Inject
import editor.helpers.EditorSpaceHelper
import editor.models.EditorUserRequest
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}


object EditorUserAction{
  val logger = Logger(this.getClass)
  def editorUserAction(org: String)(implicit ec: ExecutionContext): ActionRefiner[UserRequest, EditorUserRequest] {
    def executionContext: ExecutionContext

    def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserRequest[A]]]
  } = new ActionRefiner[UserRequest, EditorUserRequest] {
    def executionContext: ExecutionContext = ec
    def refine[A](input: UserRequest[A]): Future[Either[Result, EditorUserRequest[A]]] = Future.successful {
      if(EditorSpaceHelper.isEditorGroup(input.user, org) ){
        Right(EditorUserRequest(input.user, org, input))
      }else{
        logger.debug(s"Not allowed: ${input.user} for index: $org")
        Left(Forbidden("You are not allowed to perform this request"))
      }
    }
  }
}