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

import constants.SchemaFieldsConstants
import models.NexusPath
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes}

final case class NexusInstanceReference(nexusPath: NexusPath, id: String) {
  override def toString: String = s"${this.nexusPath.toString()}/${this.id}"
}

object NexusInstanceReference {

  def apply(org: String, domain: String, schema: String, version: String, id: String): NexusInstanceReference =
    NexusInstanceReference(NexusPath(org, domain, schema, version), id)

  def fromUrl(url: String): NexusInstanceReference = {
    val nexusId = getIdfromURL(url)
    val path = nexusId.splitAt(nexusId.lastIndexOf("/"))
    val nexusType = NexusPath(path._1.split("/").toList)
    NexusInstanceReference(nexusType, path._2.substring(1))
  }

  private def getIdfromURL(url: String): String = {
    if (url contains "v0/data/") {
      url.split("v0/data/").tail.head
    } else {
      if (url.matches("^\\w+\\/\\w+\\/\\w+\\/v+\\d+\\.\\d+\\.\\d+\\/.+$")) {
        url
      } else {
        throw new Exception(s"Could not extract id from url - $url")
      }
    }
  }

  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  implicit val nexusInstanceReferenceWrites: Writes[NexusInstanceReference] =
    (JsPath \ "id").write[String].contramap(s => s.toString)

  implicit val nexusInstanceReferenceReads: Reads[NexusInstanceReference] =
    (JsPath \ SchemaFieldsConstants.RELATIVEURL).read[String].map(s => NexusInstanceReference.fromUrl(s))

}
