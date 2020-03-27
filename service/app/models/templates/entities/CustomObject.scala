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

case class CustomObject(fieldName: String, fieldValue: Option[String]) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(CustomObject.implicitWrites)

  override type T = CustomObject

  override def zero: CustomObject = CustomObject.zero
}

object CustomObject {
  implicit val implicitWrites = new Writes[CustomObject] {

    def writes(c: CustomObject): JsValue = {
      Json.obj(
        c.fieldName -> Json.toJson(c.fieldValue)
      )
    }
  }

  def zero: CustomObject = CustomObject("", None)
}
