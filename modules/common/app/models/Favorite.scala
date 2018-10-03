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
package common.models

import play.api.libs.json._

case class Favorite(nexusId: String, instanceId: String)

object Favorite {

    import play.api.libs.functional.syntax._

    implicit val editorUserWrites = new Writes[Favorite] {
      def writes(favorite: Favorite): JsObject = Json.obj(
        "nexusId" -> favorite.nexusId.split("v0/data/").last,
        "favoriteInstance" -> favorite.instanceId,
      )
    }
    implicit val editorUserReads: Reads[Favorite] = (
      (JsPath \ "nexusId").read[String] and
        (JsPath \ "favoriteInstance").read[String]
      ) (Favorite.apply _)

}

