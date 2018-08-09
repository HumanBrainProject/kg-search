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
package models.excel_import


import play.api.libs.json.{JsArray, JsString, JsValue}
import Value._

object Value {
  def DEFAULT_UNIT = ""
  def RESOLVED = "OK"
  def NOT_FOUND = "NOT FOUND"
  def DEFAULT_RESOLUTION_STATUS = "-"
}

sealed trait Value {
  def toJson(): JsValue
  def addValue(value: String, unit: Option[String] = None): Value
  def toStringSeq(): Seq[(String, String, String)]
  def getNonEmtpy(): Option[Value]
  def resolveValue(linkRef: collection.mutable.Map[String, String]): Value
}

case class SingleValue(value: String, unit: Option[String] = None, status: Option[String] = None) extends Value{
  override def toJson(): JsValue = {
    val unitString = unit match {
      case Some(unitV) => s" $unitV"
      case None => DEFAULT_UNIT
    }
    JsString(s"$value${unitString}")
  }

  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(Seq(this, SingleValue(newValue, unit)))
  }

  override def toStringSeq(): Seq[(String, String, String)] = {
    Seq((value, unit.getOrElse(DEFAULT_UNIT), status.getOrElse(DEFAULT_RESOLUTION_STATUS)))
  }

  override def getNonEmtpy(): Option[SingleValue] = {
    if (value.trim.isEmpty) None else Some(this.copy(value = value.trim))
  }

  override def resolveValue(linksRef: collection.mutable.Map[String, String]): SingleValue = {
    linksRef.get(value) match {
      case Some(foundLink) =>
        this.copy(value = foundLink, status = Some(RESOLVED))
      case None =>
        this.copy(status =  Some(NOT_FOUND))
    }
  }
}


case class ArrayValue(values: Seq[SingleValue]) extends Value{
  override def toJson(): JsValue = {
    JsArray(values.map(_.toJson()))
  }
  override def addValue(newValue: String, unit: Option[String] = None): Value = {
    ArrayValue(values :+ SingleValue(newValue, unit))
  }

  override def toStringSeq(): Seq[(String, String, String)] = values.flatMap(_.toStringSeq())

  override def getNonEmtpy(): Option[Value] = {
    val nonEmptyValues = values.flatMap(_.getNonEmtpy())
    nonEmptyValues match {
      case Seq() => None
      case head +: Seq() => Some(nonEmptyValues.head)
      case head +: tail => Some(ArrayValue(nonEmptyValues))
    }
  }

  override def resolveValue(linksRef: collection.mutable.Map[String, String]): ArrayValue = {
    val newValues = values.map{
      value => value.resolveValue(linksRef)
    }
    this.copy(values = newValues)
  }
}
