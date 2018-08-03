
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

package data_import.helpers.excel_import

import data_import.helpers.excel_import.ExcelImportHelper.{dataFormatter, formulaEvaluator}
import models.excel_import.{Entity, SingleValue}
import org.apache.poi.ss.usermodel.{Cell, CellType}
import org.apache.poi.xssf.usermodel._
import play.api.libs.json._

import collection.JavaConverters._
object ExcelUnimindsImportHelper{

  val actionPreview = "preview"
  val actionInsert = "insert"

  val blockNameColIdx = 0
  val blockIdColIdx = 1
  val keyColIdx = 2
  val valueColIdx = 3
  val unitColIdx = 4

  val headerRowIdx = 0
  val dataFirstRowIdx = 1

  def buildJsonUnimindsDataFromExcel(workbook: XSSFWorkbook): JsObject = {
    val unimindsSheet = workbook.getSheetAt(0)

    // extract header
    val header = extractHeader(unimindsSheet)


    // extract core data from first sheet
    val data = extractCoreData(unimindsSheet)
    val entitiesByType = data.groupBy(_.`type`)

    JsObject(
      entitiesByType.map{
        case (entityType, entities) =>
          entityType -> JsArray(entities.sortWith(_.id < _.id)map(_.toJson()))
      }
    )

  }

  /*
   *  header is expected to be:
   *  blockName, blockId, key, value, unit
   */
  def extractHeader(sheet: XSSFSheet): Seq[String] = {
    sheet.getRow(headerRowIdx).asScala.map (_.getStringCellValue).toSeq
  }

  object AValidStringOrNone {
    def apply(value: String): Option[String] = {
      if (value == null || value.trim.isEmpty) None else Some(value)
    }
  }


  def getCellContentAsString(cell: Cell): String = {
    cell.getCellTypeEnum match {
      case CellType.BOOLEAN =>
        cell.getBooleanCellValue.toString
      case CellType.NUMERIC =>
        dataFormatter.formatCellValue(cell)
      case CellType.STRING =>
        cell.getStringCellValue
      case CellType.FORMULA =>
        formulaEvaluator.evaluateInCell(cell)
        getCellContentAsString(cell)
      case _ =>
        cell.toString
    }
  }

  /*
   *  exctract data from second row
   */
  def extractCoreData(sheet: XSSFSheet): Seq[Entity] = {
    sheet.asScala.tail.foldLeft(Map.empty[String, Entity]){
      case (entities, row) =>
        val blockName = getCellContentAsString(row.getCell(blockNameColIdx))
        val blockIdOpt = AValidStringOrNone(getCellContentAsString(row.getCell(blockIdColIdx)))
        val key = getCellContentAsString(row.getCell(keyColIdx))
        val value = getCellContentAsString(row.getCell(valueColIdx))
        val unit = AValidStringOrNone(getCellContentAsString(row.getCell(unitColIdx)))

        blockIdOpt match {
          case Some(blockId) =>
            val newEntity = entities.get(blockId) match{
              case Some(entity) =>
                // adding content to last entitty
                entity.addContent(key, value)
              case None =>
                // new Entitiy
                Entity(blockName, blockId, Map(key -> SingleValue(value, unit)))
            }
            entities + (blockId -> newEntity)
          case None => entities // ignore row
        }
    }.values.toSeq
  }


}