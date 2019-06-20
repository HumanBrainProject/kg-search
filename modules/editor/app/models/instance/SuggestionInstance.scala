/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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

import constants.{JsonLDConstants, SchemaFieldsConstants}
import play.api.libs.json._

final case class SuggestionInstance(
  originalRef: NexusInstanceReference,
  ref: NexusInstanceReference,
  content: JsObject,
  instanceSuggestionRef: NexusInstanceReference
)

object SuggestionInstance {

  import play.api.libs.functional.syntax._

  implicit val suggestReads: Reads[SuggestionInstance] = (
    (JsPath \ JsonLDConstants.ID).read[String].map(NexusInstanceReference.fromUrl) and
    (JsPath \ JsonLDConstants.ID)
      .read[String]
      .map(NexusInstanceReference.fromUrl)
      .map(r => r.copy(nexusPath = r.nexusPath.copy(org = r.nexusPath.org.replace("editorsug", "")))) and
    JsPath.read[JsObject] and
    (JsPath \ SchemaFieldsConstants.SUGGESTION_OF)
      .read[JsValue]
      .map(js => NexusInstanceReference.fromUrl((js \ JsonLDConstants.ID).as[String]))
  )(SuggestionInstance.apply _)
}
