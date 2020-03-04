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

import org.apache.poi.ss.usermodel.{Cell, CellType, DataFormatter}
import org.apache.poi.xssf.usermodel._
import models.excel.CommonVars._

object ExcelImportHelper {

  val actionPreview = "preview"
  val actionInsert = "insert"
  val dataFormatter = new DataFormatter()
  var formulaEvaluator = new XSSFWorkbook().getCreationHelper().createFormulaEvaluator()

  def nbFilledCellsInRow(row: XSSFRow): Int = {
    var filledCell = 0
    val it = row.cellIterator()
    while (it.hasNext) {
      if (getCellContentAsString(it.next.asInstanceOf[XSSFCell]) != empty) filledCell += 1
    }
    filledCell
  }

  def getCellContentAsString(cell: Cell): String = {
    if (cell != null) {
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
    } else { "" }
  }

  def isEmptyRow(row: XSSFRow): Boolean = {
    val it = row.cellIterator()
    while (it.hasNext()) {
      if (getCellContentAsString(it.next().asInstanceOf[XSSFCell]) != empty) {
        return false
      }
    }
    true
  }
}
