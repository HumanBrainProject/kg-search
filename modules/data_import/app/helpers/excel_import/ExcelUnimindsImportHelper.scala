
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

import models.excel_import.{Entity, SingleValue}
import org.apache.poi.xssf.usermodel._
import play.api.libs.json._
import nexus.services.NexusService._

import collection.JavaConverters._
import ExcelImportHelper._
import dataimport.helpers.excel_import.InsertionHelper

import scala.concurrent.{ExecutionContext, Future}
import data_import.services.InsertionService
import nexus.services.NexusService


object ExcelUnimindsImportHelper{

  val blockNameColIdx = 0
  val blockIdColIdx = 1
  val keyColIdx = 2
  val valueColIdx = 3
  val unitColIdx = 4
  val headerRowIdx = 0
  val dataFirstRowIdx = 1


  def buildJsonUnimindsDataFromExcel(data: Seq[Entity]): JsObject = {

    val dataRef = data.map(e => (e.id, e)).toMap
    val insertSeq = InsertionHelper.buildInsertableEntitySeq(dataRef)

    JsObject(
      data.groupBy(_.`type`).map{
        case (entityType, entities) =>
          entityType -> JsArray(entities.sortWith(_.id < _.id)map(_.toJson()))
      } ++ Map("insertableSeq" -> JsArray(
        insertSeq.map(e => JsString(e.id)))
      )
    )
  }

  def insertUnimindsDataInKG(nexusEndPoint: String, data: Seq[Entity], token: String, nexusService: NexusService, insertionService: InsertionService)
                            (implicit executionContext: ExecutionContext): Future[Seq[String]] = {

    val dataRef = data.map(e => (e.id, e)).toMap
    val insertSeq = InsertionHelper.buildInsertableEntitySeq(dataRef)

    // create schemas if needed
    val schemas = data.map(_.`type`).distinct
    schemas.foldLeft (Future.successful("")) {
      case (_, schema) =>
        nexusService.createSimpleSchema(nexusEndPoint, "uniminds", "core", schema, "v0.0.1", token).map{
          response =>
            s"${response.status}: ${response.body}"
        }
    }

    // insert entities
    val linksRef = collection.mutable.Map.empty[String, String]
    insertSeq.foldLeft(Future.successful(Seq.empty[String])) {
      case (statusSeqFut, entity) =>
        statusSeqFut.flatMap {
          statusSeq =>
            insertionService.insertEntity(nexusEndPoint, entity, linksRef, token, nexusService).map {
              insertionResponse => insertionResponse match {
                  case Left((operation, jsonResponse)) =>
                    val instanceLink = (jsonResponse \ "@id").as[String]
                    linksRef.put(entity.id, instanceLink)
                    operation match {
                      case SKIP =>
                        statusSeq :+ s"[${entity.`type`}] ${entity.id}: NO CHANGE"
                      case INSERT | UPDATE =>
                        statusSeq :+ s"[${entity.`type`}] ${entity.id} --> ${operation} OK, id: $instanceLink"
                    }
                  case Right(insertionError) =>
                    statusSeq :+ insertionError
                }
            }
        }
    }
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
      if (value == null || value.trim.isEmpty) None else Some(value.trim)
    }
  }


  /*
   *  exctract data from second row
   */
  def extractCoreData(sheet: XSSFSheet): Seq[Entity] = {
    sheet.asScala.tail.foldLeft(Map.empty[String, Entity]){
      case (entities, row) =>
        val blockName = getCellContentAsString(row.getCell(blockNameColIdx)).trim
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