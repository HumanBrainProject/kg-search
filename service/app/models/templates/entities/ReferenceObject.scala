package models.templates.entities

import play.api.libs.json.{JsValue, Json}

case class ReferenceObject(reference: Option[String]) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(ReferenceObject.implicitWrites)

  override type T = ReferenceObject

  override def zero: ReferenceObject = ReferenceObject.zero

  def map(f: String => String): ReferenceObject = {
    ReferenceObject(reference.map(f))
  }
}

object ReferenceObject {
  implicit val implicitWrites = Json.writes[ReferenceObject]
  def zero: ReferenceObject = ReferenceObject(None)
}
