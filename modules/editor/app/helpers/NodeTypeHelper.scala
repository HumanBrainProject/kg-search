
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

package helpers

import models.NexusPath
import play.api.libs.json.{JsArray, Json}

object NodeTypeHelper {

  def formatNodetypeList(jsArray: JsArray): JsArray = {
  val r = jsArray.value.map { js =>
  val tempId = (js \ "schema" \ "value").as[String]
  val path: String = tempId.split("v0/schemas/").tail.head
  val label = NexusPath.apply(path.split("/").toList).schema
  Json.obj("label" -> label.capitalize, "path" -> path)
}.sortBy( js => (js \ "label").as[String])(Ordering.fromLessThan[String]( _ < _ ))
  Json.toJson(r).as[JsArray]
}
}
