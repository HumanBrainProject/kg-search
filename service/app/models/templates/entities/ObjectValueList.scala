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
import play.api.libs.json.{JsValue, Json, Writes}

case class ObjectValueList(list: List[TemplateEntity]) extends TemplateEntity {
  override type T = ObjectValueList

  override def zero: ObjectValueList = ObjectValueList.zero

  override def toJson: JsValue = Json.toJson(list)

  def :+(el: TemplateEntity): ObjectValueList = {
    this.copy(list :+ el)
  }
}

object ObjectValueList {
  implicit lazy val implicitWrites = new Writes[ObjectValueList] {

    def writes(c: ObjectValueList): JsValue = {
      Json.toJson(c.list.map { el =>
        el match {
          case j: ValueObjectString => Json.toJson(j)(ValueObjectString.implicitWrites)
          case j: UrlObject         => Json.toJson(j)(UrlObject.implicitWrites)
          case j: ValueObjectList   => Json.toJson(j)(ValueObjectList.implicitWrites)
          case j: NestedObject      => Json.toJson(j)(NestedObject.implicitWrites)
          case j: ObjectValueMap    => Json.toJson(j)(ObjectValueMap.implicitWrites)
        }
      })
    }
  }

  def zero = ObjectValueList(List())

}
