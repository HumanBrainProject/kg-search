
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
package editor.services

import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.NexusUser
import common.services.ConfigurationService
import editor.models.EditorUserList.{NODETYPE, UserFolder, UserList}
import nexus.services.NexusService
import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import services.FormService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EditorUserListService @Inject()(config: ConfigurationService,
                                      wSClient: WSClient,
                                      formService: FormService,
                                 )(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)
  def getUserLists(user: NexusUser):Future[Either[WSResponse, List[UserFolder]]] = {
    // Query lists from arango

    // Concatenate with form service lists
    val r = getEditableEntities(user)
    Future(Right(r))
  }

  private def getEditableEntities(user: NexusUser) = {
    val allEditableEntities = FormService.editableEntities(user, formService.formRegistry)
      .foldLeft( (List[UserList](), List[UserList]())){
        case (acc, userList) =>
          if(EditorUserListService.commonNodeTypes.contains(userList.id)){
            (userList :: acc._1, acc._2)
          }else{
            (acc._1, userList :: acc._2)
          }
      }
    List(
      UserFolder(
        "Common node types",
        NODETYPE,
        allEditableEntities._1
      ),
      UserFolder(
        "Other node types",
        NODETYPE,
        allEditableEntities._2
      )
    )
  }

}

object EditorUserListService {
  val commonNodeTypes = List("minds/core/dataset/v0.0.4")
}
