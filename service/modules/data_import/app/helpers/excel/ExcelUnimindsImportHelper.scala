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

import collection.JavaConverters._
import models.excel.{Entity, SingleValue}
import org.apache.poi.xssf.usermodel._
import ExcelImportHelper._
import models.NexusPath

object ExcelUnimindsImportHelper {

  val BLOCK_NAME_COL_IDX = 0
  val BLOCK_ID_COL_IDX = 1
  val KEY_COL_IDX = 2
  val VALUE_COL_IDX = 3
  val UNIT_COL_IDX = 4
  val unimindsOrg = "unimindsexcel"
  val unimindsDomain = "core"
  val unimindsVersion = "v0.0.5"

  object AValidStringOrNone {

    def apply(value: String): Option[String] = {
      if (value == null || value.trim.isEmpty) None else Some(value.trim)
    }
  }

  /*
   *  exctract data from second row
   */
  def extractCoreData(sheet: XSSFSheet): Seq[Entity] = {
    sheet.asScala.tail
      .foldLeft(Map.empty[String, Entity]) {
        case (entities, row) =>
          val blockName = getCellContentAsString(row.getCell(BLOCK_NAME_COL_IDX)).trim
          val blockIdOpt = AValidStringOrNone(getCellContentAsString(row.getCell(BLOCK_ID_COL_IDX)))
          val key = getCellContentAsString(row.getCell(KEY_COL_IDX))
          val value = getCellContentAsString(row.getCell(VALUE_COL_IDX))
          val unit = AValidStringOrNone(getCellContentAsString(row.getCell(UNIT_COL_IDX)))

          blockIdOpt match {
            case Some(blockId) =>
              val newEntity = entities.get(blockId) match {
                case Some(entity) =>
                  // adding content to last entity
                  entity.addContent(key, value, unit)
                case None =>
                  // new Entitiy
                  val pathOpt = blockName.toLowerCase.split("/").toList match {
                    case domain :: schema :: Nil =>
                      Some(
                        NexusPath(
                          ExcelUnimindsImportHelper.unimindsOrg,
                          domain.toLowerCase,
                          schema.toLowerCase,
                          ExcelUnimindsImportHelper.unimindsVersion
                        )
                      )
                    case schema :: Nil =>
                      Some(
                        NexusPath(
                          ExcelUnimindsImportHelper.unimindsOrg,
                          ExcelUnimindsImportHelper.unimindsDomain,
                          schema,
                          ExcelUnimindsImportHelper.unimindsVersion
                        )
                      )
                    case _ => None
                  }
                  Entity(blockName, blockId, Map(key -> SingleValue(value, unit)), pathOpt)
              }
              entities + (blockId -> newEntity)
            case None => entities // ignore row
          }
      }
      .values
      .toSeq
  }
}
