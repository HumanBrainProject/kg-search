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


import java.awt.Color

import data_import.helpers.excel_import.ExcelImportHelper
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import Entity._
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet


case class Entity(rawType: String, id: String, rawContent: Map[String, Value], status: Option[String] = None){
  def `type` = rawType.toLowerCase.trim

  // make key valid
  def buildValidKey(key: String): String = {
    key.trim.replace(" ", "_")
  }

  // filter empty content
  def content = rawContent.flatMap {
    case (key, value) => value.getNonEmtpy() match {
      case Some(nonEmptyValue) => Some((buildValidKey(key), nonEmptyValue))
      case None => None
    }
  }

  def toJson(): JsObject = {
    JsObject( Map(
      id -> JsObject.apply(
        content.map{
          case (key, value) => (key, value.toJson())
        }
      )
    ))
  }

  def toKGPayload(linksRef: collection.mutable.Map[String,String]): JsObject = {
    val originalContent = content.flatMap{
      case (key, value) =>
        if (isLink(key)) {
          val links = value.toStringSeq().flatMap(tuple => linksRef.get(tuple._1))
          if (links.isEmpty) {
            None
          } else {
            val linksBlock: JsValue = links.size match {
              case 1 =>
                JsObject(Map(
                  "@id" -> JsString(links.head)))
              case _ =>
                JsArray(
                  links.map(link => JsObject(Map("@id" -> JsString(link)))))
            }
            Some((s"http://hbp.eu/uniminds#$key", linksBlock))
          }
        } else {
          Some((key, value.toJson()))
        }
    }.toSeq
    val hashCode = JsString(ExcelImportHelper.hash(originalContent.toString()))
    val identifier = JsString(ExcelImportHelper.hash(s"${`type`}$id"))
    JsObject(
      originalContent :+
      ("@type", JsString(s"http://hbp.eu/uniminds#${`type`}")) :+
      ("http://schema.org/identifier", identifier) :+
      ("http://hbp.eu/internal#hashcode", hashCode)
    )
  }

  def resolveLinksAndStatus(linksRef: collection.mutable.Map[String,String], newStatus: Option[String] = None): Entity = {
    val idLink = linksRef.getOrElse(id, EMPTY_LINK)

    val newContent = rawContent.map{
      case (key, value) =>
        if (isLink(key)) {
          val newValue = value.resolveValue(linksRef)
          (key, newValue)
        } else {
          (key, value)
        }
    }.+((ID_LABEL, SingleValue(idLink, None, newStatus)))
    this.copy(rawContent = newContent, status = newStatus)
  }

  /*
   * insert entity data at idx row and return index of next row to be used
   */
  def toExcelRows(sheet: XSSFSheet, idx: Int, styleOpt: Option[CellStyle] = None): Int =  {
    content.toSeq.sortWith(_._1 < _._1).foldLeft(0) {
      case (count, (key, value)) =>
        val valuesString = value.toStringSeq()
        valuesString.foldLeft(0) {
          case (subcount, (valueString, unit, status)) =>
            val row = sheet.createRow(idx+count+subcount)
            Seq(`type`, id, key, valueString, unit, status).zipWithIndex.foreach {
              case (value, colIdx) =>
                val cell = row.createCell(colIdx)
                cell.setCellValue(value)
                styleOpt.map(cell.setCellStyle(_))
            }
            subcount + 1
        } + count
    } + idx
  }

  /*
   * header is: block name / block id /	key /	value /	unit of value / status
   */
  def toCsv(): Seq[String] = {
    content.toSeq.sortWith(_._1 < _._1).flatMap{
      case (key, value) =>
        val valuesString = value.toStringSeq()
        valuesString.map {
          case (valueString, unit, status) =>
            s"${Seq(`type`, id, key, valueString, unit, status).mkString(CSV_SEP)}\n"
        }
    }
  }


  def addContent(key: String, newValue: String, unit: Option[String] = None): Entity = {
    SingleValue(newValue, unit).getNonEmtpy() match {
      case Some(nonEmptyValue) =>
        val newContent = content.get(key) match {
          case Some(value) =>
            content + (key -> value.addValue(newValue, unit))
          case None =>
            content + (key -> nonEmptyValue)
        }
        this.copy(rawContent=newContent)
      case None => this
    }
  }

  def isLink(key: String): Boolean = {
    key.startsWith(LINK_PREFIX) && key != ID_LABEL
  }

  def getLinkedIds(): Seq[String] = {
    content.collect{
      case (key, value) if isLink(key) =>
          value.toStringSeq().map(_._1)
    }.flatten.toSeq
  }
}

object Entity {
  val LINK_PREFIX = "_"
  val ID_LABEL = "_ID"
  val EMPTY_LINK = "./"
  val CSV_SEP = "^"
}
