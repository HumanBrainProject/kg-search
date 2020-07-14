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

case class ObjectWithCustomField[ReturnValue](fieldName: String, fieldValue: Option[ReturnValue])(
  implicit writes: Writes[ReturnValue]
) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(ObjectWithCustomField.implicitWrites)

}

object ObjectWithCustomField {
  implicit def implicitWrites[T: Writes](implicit returnTypeWrite: Writes[T]): Writes[ObjectWithCustomField[T]] =
    new Writes[ObjectWithCustomField[T]] {

      def writes(c: ObjectWithCustomField[T]): JsValue = {
        c.fieldValue.fold[JsValue](JsNull)(
          v =>
            Json.obj(
              c.fieldName -> Json.toJson(v)(returnTypeWrite)
          )
        )
      }
    }

  def zero[T: Writes]: ObjectWithCustomField[T] = ObjectWithCustomField[T]("", None)
}
