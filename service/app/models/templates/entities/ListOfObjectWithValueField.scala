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
package models.templates.entities
import play.api.libs.json.{Format, JsNull, JsValue, Json, Writes}

case class ListOfObjectWithValueField[ReturnType: Format](list: List[ObjectWithValueField[ReturnType]])
    extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(ListOfObjectWithValueField.implicitWrites)

}

object ListOfObjectWithValueField {
  def zero[ReturnType: Format]: ListOfObjectWithValueField[ReturnType] = ListOfObjectWithValueField[ReturnType](List())
  implicit def implicitWrites[ReturnType: Format]: Writes[ListOfObjectWithValueField[ReturnType]] =
    new Writes[ListOfObjectWithValueField[ReturnType]] {

      def writes(u: ListOfObjectWithValueField[ReturnType]): JsValue = {
        if (u.list.isEmpty) {
          JsNull
        } else {
          Json.toJson(u.list.map(el => el.toJson))
        }
      }
    }
}
