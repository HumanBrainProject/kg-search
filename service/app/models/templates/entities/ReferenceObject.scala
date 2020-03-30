package models.templates.entities

import play.api.libs.json.{JsNull, JsValue, Json, Writes}

case class ReferenceObject(reference: Option[String]) extends TemplateEntity {
  override def toJson: JsValue = Json.toJson(this)(ReferenceObject.implicitWrites)

  override type T = ReferenceObject

  override def zero: ReferenceObject = ReferenceObject.zero

  def map(f: String => String): ReferenceObject = {
    ReferenceObject(reference.map(f))
  }
}

object ReferenceObject {
  implicit val implicitWrites: Writes[ReferenceObject] = new Writes[ReferenceObject] {

    def writes(u: ReferenceObject): JsValue = {
      u.reference.fold[JsValue](JsNull)(v => Json.obj("reference" -> Json.toJson(v)))
    }
  }
  def zero: ReferenceObject = ReferenceObject(None)
}
