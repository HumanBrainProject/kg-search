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

import play.api.libs.json.{JsObject, JsValue, Json}

final case class EditorFieldSpecification(
  key: String,
  label: String,
  instancesPath: Option[String],
  fieldType: FieldType,
  closeDropdownAfterInteraction: Option[Boolean],
  mappingValue: Option[JsValue],
  mappingLabel: Option[String],
  mappingReturn: Option[JsValue],
  isLink: Option[Boolean],
  allowCustomValues: Option[Boolean],
  isReverse: Option[Boolean] = None,
  reverseTargetField: Option[String] = None,
  isLinkingInstance: Option[Boolean] = None,
  linkingInstanceType: Option[String] = None,
  linkingInstancePath: Option[String] = None,
  value: Option[JsValue] = None,
  uiDirective: Option[JsObject] = None
) {

  def transformToNormalizedJsonStructure(): JsObject = {
    val json = Json.toJson(this).as[JsObject]
    if (uiDirective.isDefined) {
      val fieldWithDirectives = uiDirective.get.fields.foldLeft(json) {
        case (f, (k, v)) =>
          f ++ Json.obj(k -> v)
      }
      Json.obj(key -> (fieldWithDirectives - "uiDirective" - "key"))
    } else {
      Json.obj(key -> (json - "key"))
    }
  }
}

object EditorFieldSpecification {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val EditorFieldSpecificationWrites: Writes[EditorFieldSpecification] = (
    (JsPath \ "key").write[String] and
    (JsPath \ "label").write[String] and
    (JsPath \ "instancesPath").writeNullable[String] and
    (JsPath \ "type").write[FieldType] and
    (JsPath \ "closeDropdownAfterInteraction").writeNullable[Boolean] and
    (JsPath \ "mappingValue").writeNullable[JsValue] and
    (JsPath \ "mappingLabel").writeNullable[String] and
    (JsPath \ "mappingReturn").writeNullable[JsValue] and
    (JsPath \ "isLink").writeNullable[Boolean] and
    (JsPath \ "allowCustomValues").writeNullable[Boolean] and
    (JsPath \ "isReverse").writeNullable[Boolean] and
    (JsPath \ "reverseTargetField").writeNullable[String] and
    (JsPath \ "isLinkingInstance").writeNullable[Boolean] and
    (JsPath \ "linkingInstanceType").writeNullable[String] and
    (JsPath \ "linkingInstancePath").writeNullable[String] and
    (JsPath \ "value").writeNullable[JsValue] and
    (JsPath \ "uiDirective").writeNullable[JsObject]
  )(unlift(EditorFieldSpecification.unapply))

  implicit val EditorFieldSpecificationReads: Reads[EditorFieldSpecification] = (
    (JsPath \ "key").read[String] and
    (JsPath \ "label").read[String] and
    (JsPath \ "instancesPath").readNullable[String] and
    (JsPath \ "type").read[String].map(FieldType(_)) and
    (JsPath \ "closeDropdownAfterInteraction").readNullable[Boolean] and
    (JsPath \ "mappingValue").readNullable[JsValue] and
    (JsPath \ "mappingLabel").readNullable[String] and
    (JsPath \ "mappingReturn").readNullable[JsValue] and
    (JsPath \ "isLink").readNullable[Boolean] and
    (JsPath \ "allowCustomValues").readNullable[Boolean] and
    (JsPath \ "isReverse").readNullable[Boolean] and
    (JsPath \ "reverseTargetField").readNullable[String] and
    (JsPath \ "isLinkingInstance").readNullable[Boolean] and
    (JsPath \ "linkingInstanceType").readNullable[String] and
    (JsPath \ "linkingInstancePath").readNullable[String] and
    (JsPath \ "value").readNullable[JsValue] and
    (JsPath \ "uiDirective").readNullable[JsObject]
  )(EditorFieldSpecification.apply _)
}
