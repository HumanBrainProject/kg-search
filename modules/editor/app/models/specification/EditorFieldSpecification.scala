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

import play.api.libs.json.JsValue

case class EditorFieldSpecification(
  label: String,
  instancesPath: Option[String],
  fieldType: FieldType,
  closeDropdownAfterInteraction: Option[Boolean],
  mappingValue: Option[String],
  mappingLabel: Option[String],
  isLink: Option[Boolean],
  allowCustomValues: Option[Boolean],
  isReverse: Option[Boolean] = None,
  reverseTargetField: Option[String] = None,
  isLinkingInstance: Option[Boolean] = None,
  value: Option[JsValue] = None
)

object EditorFieldSpecification {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val EditorFieldSpecificationWrites: Writes[EditorFieldSpecification] = (
    (JsPath \ "label").write[String] and
    (JsPath \ "instancesPath").writeNullable[String] and
    (JsPath \ "type").write[FieldType] and
    (JsPath \ "closeDropdownAfterInteraction").writeNullable[Boolean] and
    (JsPath \ "mappingValue").writeNullable[String] and
    (JsPath \ "mappingLabel").writeNullable[String] and
    (JsPath \ "isLink").writeNullable[Boolean] and
    (JsPath \ "allowCustomValues").writeNullable[Boolean] and
    (JsPath \ "isReverse").writeNullable[Boolean] and
    (JsPath \ "reverseTargetField").writeNullable[String] and
    (JsPath \ "isLinkingInstance").writeNullable[Boolean] and
    (JsPath \ "value").writeNullable[JsValue]
  )(unlift(EditorFieldSpecification.unapply))

  implicit val EditorFieldSpecificationReads: Reads[EditorFieldSpecification] = (
    (JsPath \ "label").read[String] and
    (JsPath \ "instancesPath").readNullable[String] and
    (JsPath \ "type").read[String].map(FieldType(_)) and
    (JsPath \ "closeDropdownAfterInteraction").readNullable[Boolean] and
    (JsPath \ "mappingValue").readNullable[String] and
    (JsPath \ "mappingLabel").readNullable[String] and
    (JsPath \ "isLink").readNullable[Boolean] and
    (JsPath \ "allowCustomValues").readNullable[Boolean] and
    (JsPath \ "isReverse").readNullable[Boolean] and
    (JsPath \ "reverseTargetField").readNullable[String] and
    (JsPath \ "isLinkingInstance").readNullable[Boolean] and
    (JsPath \ "value").readNullable[JsValue]
  )(EditorFieldSpecification.apply _)
}
