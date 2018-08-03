
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

package models.excel_import

import play.api.libs.json.JsObject

case class Entity(`type`: String, id: String, content: Map[String, Value]){

  def toJson(): JsObject = {
    JsObject( Map(
      id -> JsObject.apply(
        content.map{
          case (key, value) => (key, value.toJson())
        }
      )
    ))
  }

  def addContent(key: String, newValue: String): Entity = {
    val newContent = content.get(key) match {
      case Some(value) =>
        content + (key -> value.addValue(newValue))
      case None =>
        content + (key -> SingleValue(newValue))
    }
    this.copy(content=newContent)
  }
}
