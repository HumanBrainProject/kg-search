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
package models.templates.entities

import play.api.libs.json.{JsValue, Json}

case class ValueObject(value: Option[String]) extends TemplateEntity {
  override type T = ValueObject

  def map[B](t: String => String): ValueObject = {
    ValueObject(value.map(t))
  }
  override def toJson: JsValue = Json.toJson(this)(ValueObject.implicitWrites)

  override def zero: ValueObject = ValueObject.zero
}

object ValueObject {
  implicit val implicitWrites = Json.writes[ValueObject]
  def zero: ValueObject = ValueObject(None)
}
