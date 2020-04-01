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
import play.api.libs.json.{JsNull, JsObject, JsValue, Json, OWrites, Writes}
trait ESEntity extends TemplateEntity

case class EmptyESEntity() extends ESEntity {
  override type T = EmptyESEntity

  override def zero: EmptyESEntity = EmptyESEntity()

  override def toJson: JsValue = Json.toJson(this)(EmptyESEntity.implicitWrites)
}

object EmptyESEntity {
  implicit lazy val implicitWrites: Writes[EmptyESEntity] = new Writes[EmptyESEntity] {
    def writes(u: EmptyESEntity): JsValue = JsNull
  }
}

case class ESPropertiesObject(value: ESPropertyObject, properties: List[ESPropertyObject]) extends ESEntity {
  override type T = ESPropertiesObject

  override def zero: ESPropertiesObject = ESPropertiesObject.zero

  override def toJson: JsValue = Json.toJson(this)(ESPropertiesObject.implicitWrites)

}

object ESPropertiesObject {

  def zero: ESPropertiesObject = ESPropertiesObject(ESPropertyObject(), List())

  def defaultValue: ESPropertiesObject =
    ESPropertiesObject(value = ESPropertyObject.defaultValue, List())

  implicit lazy val implicitWrites: Writes[ESPropertiesObject] = (u: ESPropertiesObject) =>
    u.properties match {
      case props =>
        Json.obj(
          "properties" -> props
            .foldLeft(Json.obj()) { case (obj, el) => obj ++ el.toJson.as[JsObject] }
            .++(u.value.toJson.as[JsObject])
        )
      case Nil => Json.obj("properties" -> u.value.toJson)
  }
}

case class ESPropertyObject(fieldName: String = "value", valueContent: ESValue = ESValue()) extends ESEntity {

  override type T = ESPropertyObject

  override def zero: ESPropertyObject = ESPropertyObject.zero

  override def toJson: JsValue = Json.toJson(this)(ESPropertyObject.implicitWrites)
}

object ESPropertyObject {
  def zero: ESPropertyObject = ESPropertyObject("")
  def defaultValue: ESPropertyObject = ESPropertyObject(valueContent = ESValue(fields = Some(ESFields(ESKeyword()))))
  implicit lazy val implicitWrites: Writes[ESPropertyObject] = (u: ESPropertyObject) =>
    Json.obj(u.fieldName -> u.valueContent.toJson)
}

case class ESFields(keyword: ESKeyword)

object ESFields {
  implicit val implicitWrites: OWrites[ESFields] = Json.writes[ESFields]
}

case class ESKeyword(`type`: String = "keyword", ignore_above: Option[Int] = None)

object ESKeyword {
  implicit val implicitWrites: OWrites[ESKeyword] = Json.writes[ESKeyword]
}

case class ESValue(`type`: String = "text", fields: Option[ESFields] = None) {
  def toJson: JsValue = Json.toJson(this)(ESValue.implicitWrites)
}

object ESValue {
  implicit val implicitWrites: Writes[ESValue] = (u: ESValue) =>
    u.fields match {
      case Some(fields) => Json.obj("type" -> u.`type`, "fields" -> Json.toJson(fields))
      case None         => Json.obj("type" -> u.`type`)
  }
}
