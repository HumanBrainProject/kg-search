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
package editor.models

import editor.models.EditorUserList.UserFolder
import play.api.libs.json._


case class EditorUser(nexusId: String, id: String, userFolders: List[UserFolder])

object EditorUser {


  import play.api.libs.functional.syntax._

  implicit val editorUserWrites = new Writes[EditorUser] {
    def writes(user: EditorUser) = Json.obj(
      "nexusId" -> user.nexusId,
      "userId" -> user.id,
      "userFolders" -> Json.toJson(user.userFolders)
    )
  }

  implicit val editorUserReads: Reads[EditorUser] = (
    (JsPath \ "nexusId").read[String] and
      (JsPath \ "userId").read[String] and
      (JsPath \ "userFolders").read[List[UserFolder]]
        .orElse(Reads.pure(List[UserFolder]()))
    ) (EditorUser.apply _)

}
