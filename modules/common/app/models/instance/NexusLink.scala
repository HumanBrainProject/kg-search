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

import constants.{JsonLDConstants, NexusConstants}
import play.api.libs.json.{JsObject, JsPath, Json, Reads}

final case class NexusLink(ref: NexusInstanceReference) {

  def toJson(currentSystemBaseUrl: String): JsObject =
    Json.obj(JsonLDConstants.ID -> s"$currentSystemBaseUrl/${NexusConstants.dataPath}${ref.toString}")
}

object NexusLink {
  implicit val readNexusLink: Reads[NexusLink] =
    (JsPath \ JsonLDConstants.ID).read[String].map(s => NexusLink(NexusInstanceReference.fromUrl(s)))
}
