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
package models.instance

import constants.{EditorConstants, JsonLDConstants, SchemaFieldsConstants, UiConstants}
import models.NexusPath
import models.specification.{FormRegistry, UISpec}

final case class PreviewInstance(
  id: NexusInstanceReference,
  name: String,
  instanceType: String,
  description: Option[String],
  label: Option[String]
) {

  def setLabel(formRegistry: FormRegistry[UISpec]): PreviewInstance = {
    val label = formRegistry.registry.get(id.nexusPath).map(_.label).getOrElse(id.nexusPath.toString())
    this.copy(label = Some(label))
  }

}

object PreviewInstance {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def fromNexusInstance(nexusInstance: NexusInstance, formRegistry: FormRegistry[UISpec]): PreviewInstance = {
    val id = NexusInstanceReference.fromUrl(nexusInstance.getField(JsonLDConstants.ID).get.as[String])
    val name = nexusInstance.getField(SchemaFieldsConstants.NAME).getOrElse(JsString("")).as[String]
    val description: Option[String] = nexusInstance.getField(SchemaFieldsConstants.DESCRIPTION).map(_.as[String])
    val t = nexusInstance.getField(JsonLDConstants.TYPE).get.as[String]
    val label =
      formRegistry.registry.get(nexusInstance.nexusPath).map(_.label).getOrElse(nexusInstance.nexusPath.toString())
    PreviewInstance(id, name, t, description, Some(label))
  }

  implicit val previewInstanceReads: Reads[PreviewInstance] = (
    (JsPath \ "id").read[String].map(ref => NexusInstanceReference.fromUrl(ref)) and
    (JsPath \ "name").readNullable[JsValue].map {
      case Some(js) =>
        if (js.asOpt[List[String]].isDefined) js.as[List[String]].head else js.as[String]
      case None => ""
    } and
    (JsPath \ UiConstants.DATATYPE).readNullable[JsValue].map {
      case Some(js) =>
        if (js.asOpt[List[String]].isDefined) js.as[List[String]].head else js.as[String]
      case None => ""
    } and
    (JsPath \ "description").read[JsValue].map(js => js.asOpt[String]) and
    (JsPath \ UiConstants.LABEL).readNullable[String]
  )(PreviewInstance.apply _)

  implicit val previewInstanceWrites = new Writes[PreviewInstance] {

    def writes(previewInstance: PreviewInstance) = Json.obj(
      "id"                 -> previewInstance.id.toString,
      "name"               -> previewInstance.name,
      UiConstants.DATATYPE -> previewInstance.instanceType,
      "description"        -> previewInstance.description,
      UiConstants.LABEL    -> previewInstance.label,
      UiConstants.SCHEMA   -> previewInstance.id.nexusPath.toString()
    )
  }
}
