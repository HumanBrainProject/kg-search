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

package models.specification

case class UISpec(label: String,
                  fields: Map[String, EditorFieldSpecification],
                  uiInfo: Option[UIInfo] = None,
                  isEditable: Option[Boolean] = None,
                  color: Option[String] = None
                 )

object UISpec {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val UISpecReads: Reads[UISpec] = (
    (JsPath \ "label").read[String] and
      (JsPath \ "fields").read[Map[String, EditorFieldSpecification]] and
      (JsPath \ "ui_info").readNullable[UIInfo] and
      (JsPath \ "editable").readNullable[Boolean] and
      (JsPath \ "color").readNullable[String]
    ) (UISpec.apply _)

  implicit val UISpecWrites: Writes[UISpec] = (
    (JsPath \ "label").write[String] and
      (JsPath \ "fields").write[Map[String, EditorFieldSpecification]] and
      (JsPath \ "ui_info").writeNullable[UIInfo] and
      (JsPath \ "editable").writeNullable[Boolean] and
      (JsPath \ "color").writeNullable[String]
    ) (unlift(UISpec.unapply))
}