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
package helpers.excel

import helpers.excel.ExcelStyleHelper.{setAllThinBorders, setCellColor}
import java.awt.Color

import models.excel._
import org.apache.poi.xssf.usermodel.{XSSFCellStyle, XSSFColor, XSSFSheet, XSSFWorkbook}
import play.api.libs.json.JsObject

object ExcelUnimindsExportHelper {

  val GREY = new XSSFColor(new Color(0xEF, 0xEF, 0xEF))
  val WHITE = new XSSFColor(Color.WHITE)
  val CSV_HEADER = Seq("block name", "block id", "key", "value", "unit of value", "resolution status")
  val HEADER_ROW_IDX = 0
  val FIRST_DATA_ROW_IDX = 1
  val RESOLVED_SHEET_NAME = "resolved data"

  def setUnimindsLayout(sheet: XSSFSheet): Unit = {
    sheet.setColumnWidth(0, 4000)
    sheet.setColumnWidth(1, 3000)
    sheet.setColumnWidth(2, 4500)
    sheet.setColumnWidth(3, 28000)
    sheet.setColumnWidth(4, 3000)
    sheet.setColumnWidth(5, 10000)

    sheet.setDefaultRowHeight(300)
  }

  def buildArrayStyles(workbook: XSSFWorkbook): (XSSFCellStyle, XSSFCellStyle, XSSFCellStyle) = {
    val greyStyle = workbook.createCellStyle()
    setAllThinBorders(greyStyle)
    setCellColor(greyStyle, GREY)
    greyStyle.setWrapText(true)

    val whiteStyle = workbook.createCellStyle()
    setAllThinBorders(whiteStyle)
    setCellColor(whiteStyle, WHITE)
    whiteStyle.setWrapText(true)

    val headerFont = workbook.createFont()
    headerFont.setBold(true)
    val headerStyle = workbook.createCellStyle()
    setAllThinBorders(headerStyle)
    setCellColor(headerStyle, GREY)
    headerStyle.setFont(headerFont)

    (headerStyle, whiteStyle, greyStyle)
  }

  def buildExcelFromEntities(data: Seq[Entity]): XSSFWorkbook = {
    val workbook = new XSSFWorkbook()
    val resolvedSheet = workbook.createSheet(RESOLVED_SHEET_NAME)
    setUnimindsLayout(resolvedSheet)

    // styles definition
    val (headerStyle, whiteStyle, greyStyle) = buildArrayStyles(workbook)

    // create header
    val headerRow = resolvedSheet.createRow(HEADER_ROW_IDX)
    CSV_HEADER.zipWithIndex
      .foreach {
        case (value, idx) =>
          val cell = headerRow.createCell(idx)
          cell.setCellStyle(headerStyle)
          cell.setCellValue(value)
      }

    // fill data
    data.zipWithIndex.foldLeft(FIRST_DATA_ROW_IDX) {
      case (curIdx, (entity, idx)) =>
        val bgStyle = if (idx % 2 == 0) Some(whiteStyle) else Some(greyStyle)
        entity.toExcelRows(resolvedSheet, curIdx, bgStyle)
    }
    workbook
  }

  def generateEntitiesFromQuerySpec(querySpec: JsObject): Seq[Entity] = {
    querySpec.value.flatMap {
      case (_, v) =>
        v.as[JsObject].value.flatMap {
          case (_, data) =>
            data.as[JsObject].value.map {
              case (entityName, entityData) =>
                generateEntityFromQuerySpec(entityName, entityData.as[JsObject])
            }
        }
    }.toSeq
  }

  def generateEntityFromQuerySpec(entityName: String, querySpec: JsObject): Entity = {
    querySpec.value
      .map {
        case (_, data) =>
          val content = data.as[JsObject].value("fields").as[List[JsObject]].foldLeft(Map[String, Value]()) {
            case (acc, obj) =>
              val key = obj.value("label").as[String]
              val valueType = obj.value("type").as[String] match {
                case "DropdownSelect" | "GroupSelect" | "InputTextMultiple" | "DataSheet" =>
                  ArrayValue(Seq(SingleValue("Multivalue placeholder"), SingleValue("Multivalue placeholder")))
                case _ => SingleValue("Placeholder")
              }
              acc.updated(key, valueType)
          }
          Entity(entityName, "", content)
      }
      .toSeq
      .head
  }

}
