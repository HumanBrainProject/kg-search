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
import play.api.libs.json.{JsNull, JsValue, Json, Writes}

case class ListOfObject(list: List[TemplateEntity]) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(ListOfObject.implicitWrites)

  def :+(el: TemplateEntity): ListOfObject = {
    this.copy(list :+ el)
  }
}

object ListOfObject {
  implicit def implicitWrites: Writes[ListOfObject] = new Writes[ListOfObject] {

    def writes(c: ListOfObject): JsValue = {
      val js = c.list.map(el => el.toJson)
      if (js.forall(jsEntity => jsEntity == JsNull)) {
        JsNull
      } else {
        Json.toJson(js)
      }
    }
  }

  def zero: ListOfObject = ListOfObject(List())

}
