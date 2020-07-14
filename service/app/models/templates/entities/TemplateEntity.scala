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

import scala.reflect.ClassTag

trait EntitySerialization {
  def toJson: JsValue
}

trait TemplateEntity extends EntitySerialization

case class EmptyEntity() extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(EmptyEntity.implicitWrites)
}

object EmptyEntity {
  implicit lazy val implicitWrites: Writes[EmptyEntity] = new Writes[EmptyEntity] {
    def writes(u: EmptyEntity): JsValue = JsNull
  }
}

case class SetValue(fieldName: String, fieldValue: JsValue) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(SetValue.implicitWrites)
}

object SetValue {
  implicit def implicitWrites: Writes[SetValue] =
    new Writes[SetValue] {

      def writes(c: SetValue): JsValue = {
        Json.obj(
          c.fieldName -> c.fieldValue
        )
      }
    }

  def zero: SetValue = SetValue("", JsNull)
}

case class GetValue[ReturnType: Format](value: Option[ReturnType]) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(GetValue.implicitWrites[ReturnType])
}

object GetValue {
  implicit def implicitWrites[ReturnType: Writes]: Writes[GetValue[ReturnType]] =
    new Writes[GetValue[ReturnType]] {

      def writes(u: GetValue[ReturnType]): JsValue = {
        u.value.fold[JsValue](JsNull)(str => Json.toJson(str))
      }
    }
  def zero[ReturnType: Format]: GetValue[ReturnType] = GetValue[ReturnType](None)
}
