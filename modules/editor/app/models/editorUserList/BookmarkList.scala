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
package models.editorUserList

import constants.EditorConstants
import models.specification.UIInfo

final case class BookmarkList(
  id: String,
  name: String,
  editable: Option[Boolean],
  UIInfo: Option[UIInfo],
  color: Option[String]
)

object BookmarkList {

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val userListReads: Reads[BookmarkList] = (
    (JsPath \ "id").read[String].map(id => s"${EditorConstants.bookmarkListPath.toString()}/${id.split("/").last}") and
    (JsPath \ "name").read[String] and
    (JsPath \ "editable").readNullable[Boolean] and
    (JsPath \ "uiSpec").readNullable[UIInfo] and
    (JsPath \ "color").readNullable[String]
  )(BookmarkList.apply _)

  implicit val userListWrites: Writes[BookmarkList] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "name").write[String] and
    (JsPath \ "editable").writeNullable[Boolean] and
    (JsPath \ "uiSpec").writeNullable[UIInfo] and
    (JsPath \ "color").writeNullable[String]
  )(unlift(BookmarkList.unapply))
}
