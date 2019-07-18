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

import play.api.libs.json.JsObject

import scala.collection.mutable

final case class UISpec(
  label: String,
  fields: List[EditorFieldSpecification],
  uiInfo: Option[UIInfo] = None,
  uiDirective: Option[JsObject] = None,
  isEditable: Option[Boolean] = None,
  color: Option[String] = None,
  folderID: Option[String] = None,
  folderName: Option[String] = None,
  refreshSpecification: Option[Boolean] = None
) {

  def getFieldsAsLinkedMap: mutable.LinkedHashMap[String, EditorFieldSpecification] = {
    mutable.LinkedHashMap[String, EditorFieldSpecification](fields map { f =>
      (f.key, f)
    }: _*)
  }

  def getFieldsAsMap: Map[String, EditorFieldSpecification] = {
    Map[String, EditorFieldSpecification](fields map { f =>
      (f.key, f)
    }: _*)
  }
}

object UISpec {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val reads: Reads[UISpec] = (
    (JsPath \ "label").read[String] and
    (JsPath \ "fields").read[List[EditorFieldSpecification]] and
    (JsPath \ "ui_info").readNullable[UIInfo] and
      (JsPath \ "uiDirective").readNullable[JsObject] and
    (JsPath \ "editable").readNullable[Boolean] and
    (JsPath \ "color").readNullable[String] and
    (JsPath \ "folderID").readNullable[String] and
    (JsPath \ "folderName").readNullable[String] and
    (JsPath \ "refreshSpecification").readNullable[Boolean]
  )(UISpec.apply _)

  implicit val UISpecWrites: Writes[UISpec] = (
    (JsPath \ "label").write[String] and
    (JsPath \ "fields").write[List[EditorFieldSpecification]] and
    (JsPath \ "ui_info").writeNullable[UIInfo] and
      (JsPath \ "uiDirective").writeNullable[JsObject] and
    (JsPath \ "editable").writeNullable[Boolean] and
    (JsPath \ "color").writeNullable[String] and
    (JsPath \ "folderID").writeNullable[String] and
    (JsPath \ "folderName").writeNullable[String] and
    (JsPath \ "refreshSpecification").writeNullable[Boolean]
  )(unlift(UISpec.unapply))
}
