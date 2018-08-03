package models.excel_import

import play.api.libs.json.{JsArray, JsString, JsValue}

sealed trait Value {
  def defaultUnit = ""
  def toJson(): JsValue
  def addValue(value: String, unit: Option[String] = None): Value
}

case class SingleValue(value: String, unit: Option[String] = None) extends Value{
  override def toJson(): JsValue = {
    JsString(s"$value ${unit.getOrElse(defaultUnit)}")
  }

  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(Seq(this, SingleValue(newValue, unit)))
  }
}

case class ArrayValue(values: Seq[Value]) extends Value{
  override def toJson(): JsValue = {
    JsArray(values.map(_.toJson()))
  }
  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(values :+ SingleValue(newValue, unit))
  }
}
