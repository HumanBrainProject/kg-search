
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

import constants.{EditorConstants, SchemaFieldsConstants}
import services.bookmark.EditorBookmarkService

case class PreviewInstance(id: String, name:String, description:Option[String]){


}

object PreviewInstance {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def fromNexusInstance(nexusInstance: NexusInstance): PreviewInstance = {
    val id = nexusInstance.getField("@id").get.as[String]
    val name = nexusInstance.getField(SchemaFieldsConstants.NAME).getOrElse(JsString("")).as[String]
    val description: Option[String] = nexusInstance.getField(SchemaFieldsConstants.DESCRIPTION).map(_.as[String])
    PreviewInstance(id, name, description)
  }

  implicit val previewInstanceReads: Reads[PreviewInstance] = (
    (JsPath \ "id").read[String].map { id =>
      val uuid = id.split("/").last.split("\\?rev=").head
      s"${EditorConstants.bookmarkListPath.toString()}/${uuid}"
    } and
      (JsPath \ "name").read[String] and
      (JsPath \ "description").readNullable[String]
    ) (PreviewInstance.apply _)

  implicit val previewInstanceWrites: Writes[PreviewInstance] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").writeNullable[String]
    ) (unlift(PreviewInstance.unapply))
}
