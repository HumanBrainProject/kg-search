/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
package utils

import play.api.libs.json.{JsNull, JsString, JsValue}

object TemplateHelper {

  def schemaIdToSearchId(`type`: String): String => String = (schemaId: String) => {
    val uuidPattern = "(\\w+\\/\\w+\\/\\w+\\/v\\d\\.\\d\\.\\d)".r
    uuidPattern replaceFirstIn (schemaId, `type`)
  }

  def refUUIDToSearchId(`type`: String): String => String = (reference: String) => {
    s"${`type`}/$reference"
  }

  def defaultESMapping(
    fieldName: String
  ): ObjectReader[Nested[WriteObject[ESProperty]]] = {
    ObjectReader(
      fieldName,
      Nested(
        "properties",
        WriteObject(
          List(ESProperty("value"))
        )
      )
    )
  }
}
