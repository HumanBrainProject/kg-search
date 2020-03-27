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

trait EntitySerialization {
  def toJson: JsValue
}

trait TemplateEntity extends EntitySerialization {
  type T <: TemplateEntity
  def zero: T
}

object TemplateEntity {
  implicit lazy val implicitWrites = new Writes[TemplateEntity] {

    def writes(u: TemplateEntity): JsValue = u match {
      case j: ValueObjectList => Json.toJson(j)(ValueObjectList.implicitWrites)
      case j: ValueObject     => Json.toJson(j)(ValueObject.implicitWrites)
      case j: UrlObject       => Json.toJson(j)(UrlObject.implicitWrites)
      case j: ObjectValueList => Json.toJson(j)(ObjectValueList.implicitWrites)
      case j: ObjectValueMap  => Json.toJson(j)(ObjectValueMap.implicitWrites)
      case j: NestedObject    => Json.toJson(j)(NestedObject.implicitWrites)
    }
  }
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
