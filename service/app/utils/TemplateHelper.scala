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
package utils

import play.api.libs.json.{JsNull, JsObject, JsString, JsValue, Json}

object TemplateHelper {

  def schemaIdToSearchId(`type`: String): JsValue => JsValue = (schemaId: JsValue) => {
    val uuidPattern = "(\\w+\\/\\w+\\/\\w+\\/v\\d\\.\\d\\.\\d)".r
    val s = uuidPattern replaceFirstIn (schemaId.as[String], `type`)
    JsString(s)
  }

  def refUUIDToSearchId(`type`: String): JsValue => JsValue = (reference: JsValue) => {
    val refOption = for {
      refStr <- reference.asOpt[String]
    } yield JsString(s"${`type`}/$refStr")
    refOption.getOrElse(JsNull)
  }
}
