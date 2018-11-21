
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

package models

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
                               value:Option[JsValue] = None
                             )

object EditorFieldSpecification {

  import play.api.libs.json._

  implicit val editorFormInstanceWrites : Writes[EditorFieldSpecification] = new Writes[EditorFieldSpecification] {
    def writes(x: EditorFieldSpecification) = {
      Json.obj(
        "type" -> x.fieldType.t,
        "closeDropdownAfterInteraction" -> x.closeDropdownAfterInteraction,
        "label" -> x.label,
        "instancesPath" -> x.instancesPath,
        "mappingValue" -> x.mappingValue,
        "mappingLabel" -> x.mappingLabel,
        "isLink" -> x.isLink,
        "allowCustomValues" -> x.allowCustomValues,
        "isReverse" -> x.isReverse,
        "reverseTargetField" -> x.reverseTargetField,
        "value" -> x.value
      )
    }
  }

  implicit val editorFormInstanceReads : Reads[EditorFieldSpecification] = new Reads[EditorFieldSpecification] {
    def reads(value: JsValue) = {
      val label = (value \ "label").as[String]
      val closeDropdownAfterInteraction = (value \ "closeDropdownAfterInteraction").asOpt[Boolean]
      val fieldType = FieldType((value \ "type").as[String])
      val instancesPath = (value \ "instancesPath").asOpt[String]
      val mappingValue = (value \ "mappingValue").asOpt[String]
      val mappingLabel = (value \ "mappingLabel").asOpt[String]
      val isLink = (value \ "isLink").asOpt[Boolean]
      val allowCustomValues = (value \ "allowCustomValues").asOpt[Boolean]
      val isReverse = (value \ "isReverse").asOpt[Boolean]
      val reverseTargetField = (value \ "reverseTargetField").asOpt[String]
      val entityValue = (value \ "value").asOpt[JsValue]
      JsSuccess(EditorFieldSpecification(label, instancesPath, fieldType, closeDropdownAfterInteraction, mappingValue,
        mappingLabel, isLink, allowCustomValues, isReverse, reverseTargetField, entityValue ))
    }
  }
}
