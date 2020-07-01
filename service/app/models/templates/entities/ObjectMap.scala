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

import play.api.libs.json.{JsNull, JsObject, JsValue, Json, Writes}

case class ObjectMap(list: List[TemplateEntity]) extends TemplateEntity {

  override def toJson: JsValue = Json.toJson(this)(ObjectMap.implicitWrites)

  def :+(el: TemplateEntity): ObjectMap = {
    this.copy(list :+ el)
  }

  def map(f: TemplateEntity => TemplateEntity): ObjectMap = {
    this.copy(this.list.map(f))
  }
}

object ObjectMap {
  implicit lazy val implicitWrites: Writes[ObjectMap] = new Writes[ObjectMap] {

    def writes(c: ObjectMap): JsValue = {
      val resultObj = c.list.foldLeft(JsObject.empty) {
        case (json, el) =>
          val jsResult = el.toJson
          if (jsResult == JsNull || jsResult == JsObject.empty) {
            json
          } else {
            json ++ jsResult.as[JsObject]
          }
      }
      if (resultObj.value.isEmpty) {
        JsNull
      } else {
        resultObj
      }

    }
  }

  def zero = ObjectMap(List())

}