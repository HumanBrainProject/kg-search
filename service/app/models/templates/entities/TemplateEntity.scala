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

trait EntitySerialization {
  def toJson: JsValue
}

trait TemplateEntity extends EntitySerialization {
  type T <: TemplateEntity
  def zero: T
}

case class EmptyEntity() extends TemplateEntity {
  override type T = EmptyEntity

  override def zero: EmptyEntity = EmptyEntity()

  override def toJson: JsValue = Json.toJson(this)(EmptyEntity.implicitWrites)
}

object EmptyEntity {
  implicit lazy val implicitWrites = new Writes[EmptyEntity] {
    def writes(u: EmptyEntity): JsValue = JsNull
  }
}

case class DirectValue(fieldName: String, fieldValue: JsValue) extends TemplateEntity {
  override type T = DirectValue

  override def zero: T = DirectValue.zero

  override def toJson: JsValue = Json.toJson(this)(DirectValue.implicitWrites)
}

object DirectValue {
  implicit def implicitWrites: Writes[DirectValue] =
    new Writes[DirectValue] {

      def writes(c: DirectValue): JsValue = {
        Json.obj(
          c.fieldName -> c.fieldValue
        )
      }
    }

  def zero: DirectValue = DirectValue("", JsNull)
}
