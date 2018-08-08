package models.excel_import

import play.api.libs.json.{JsArray, JsString, JsValue}

sealed trait Value {
  def defaultUnit = ""
  def toJson(): JsValue
  def addValue(value: String, unit: Option[String] = None): Value
  def toStringSeq(): Seq[String]
  def getNonEmtpy(): Option[Value]
}

case class SingleValue(value: String, unit: Option[String] = None) extends Value{
  override def toJson(): JsValue = {
    val unitString = unit match {
      case Some(unitV) => s" $unitV"
      case None => defaultUnit
    }
    JsString(s"$value${unitString}")
  }

  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(Seq(this, SingleValue(newValue, unit)))
  }

  override def toStringSeq(): Seq[String] = Seq(value)

  override def getNonEmtpy(): Option[Value] = if (value.trim.isEmpty) None else Some(SingleValue(value.trim))
}

case class ArrayValue(values: Seq[Value]) extends Value{
  override def toJson(): JsValue = {
    JsArray(values.map(_.toJson()))
  }
  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(values :+ SingleValue(newValue, unit))
  }

  override def toStringSeq(): Seq[String] = values.flatMap(_.toStringSeq())

  override def getNonEmtpy(): Option[Value] = {
    val nonEmptyValues = values.flatMap(_.getNonEmtpy())
    nonEmptyValues match {
      case Seq() => None
      case head +: Seq() => Some(nonEmptyValues.head)
      case head +: tail => Some(ArrayValue(nonEmptyValues))
    }
  }
}
