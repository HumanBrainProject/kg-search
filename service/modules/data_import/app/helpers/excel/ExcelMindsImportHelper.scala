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

import models.excel._
import CommonVars._
import java.util.Calendar

import org.apache.poi.xssf.usermodel._
import play.api.libs.json._
import ExcelImportHelper._
import constants.SchemaFieldsConstants

object ExcelMindsImportHelper {

  def formatEntityPayload(payload: JsObject, entityType: String, parent: Option[JsValue] = None): JsObject = {
    val payloadId = (payload \ "Specification" \ valueLabel).as[String]
    val mainContent = Seq(
      ("@type", JsString(s"http://hbp.eu/dw#$entityType")),
      (SchemaFieldsConstants.IDENTIFIER -> JsString(payloadId)),
      ("http://hbp.eu/dw#raw_content"   -> payload)
    )
    val fullContent = parent match {
      case Some(parentObj) => mainContent :+ ("http://hbp.eu/dw#isLinkedWith", parentObj)
      case None            => mainContent
    }
    JsObject(fullContent)
  }

  def extractGlobalInfo(sheet: XSSFSheet): Map[String, String] = {
    // extract global info from the first rows
    (globalInfoFirstIdx to globalInfoLastIdx).flatMap {
      case rowIdx =>
        val row = sheet.getRow(rowIdx)
        val key = getCellContentAsString(row.getCell(globalInfoKeyIdx))
        if (key != empty) {
          Some(key, getCellContentAsString(row.getCell(globalInfoValueIdx)))
        } else {
          None
        }
    }.toMap
  }

  def buildJsonMindsDataFromExcel(workbook: XSSFWorkbook): JsObject = {
    val insertionDateTime = Calendar.getInstance.getTime.toString
    val mindsSheet = workbook.getSheet(mindsLabel)

    // extract global info
    val globalInfo = extractGlobalInfo(mindsSheet)

    // extract core data from MINDS sheet
    val blockList = getContentBlocks(mindsSheet)
    // allow children of a given section name to be split in different section blocks
    val res = blockList.foldLeft(Map.empty[String, Seq[Map[String, Seq[String]]]]) {
      case (resultMap, (sectionTitle, from, to)) =>
        val currentSectionData = buildJsonData(from, to, mindsSheet)
        val prevEntry = resultMap.getOrElse(sectionTitle, Seq.empty[Map[String, Seq[String]]])
        resultMap + ((sectionTitle, prevEntry ++ currentSectionData))
    }

    /*
     * build Dataset, SpecimentGroup and Activity from excel data
     */
    // retrieve shared info from pla
    val componentId = res(plaSectionLabel).head(componentIdLabel).head

    // dataset
    val creationDate = globalInfo("Date:")
    val creator = globalInfo("Email:")
    val description = globalInfo("Description:")

    val datasetId = res(datasetSectionLabel).head(datasetIdLabel).head
    val datasetContent = Dataset(datasetId, description, res(datasetSectionLabel).head).toJsonString()
    val coreDatasetJson = buildJsonEntity(datasetContent, insertionDateTime, creator, componentId)

    // activity
    val activityId = res(activitySectionLabel).head(activityIdLabel).head
    val activityContent = Activity(activityId, res(activitySectionLabel).head).toJsonString()
    val coreActivityJson = buildJsonEntity(activityContent, insertionDateTime, creator, componentId)

    // specimengroup
    val samplesGroups = res(sampleSectionLabel)
      .map {
        case sampleDetails =>
          Sample(sampleDetails(sampleIdLabel).head, sampleDetails)
      }
      .groupBy(_.details(subjectIdLabel).head)

    val subjects = res(specimenGroupSectionLabel).map {
      case specimenGroups =>
        val specimenId = specimenGroups(subjectIdLabel).head
        Subject(specimenId, specimenGroups, samplesGroups(specimenId))
    }

    val specimenGroupId = res(specimenGroupSectionLabel).head(specimenGroupIdLabel).head
    val specimenGroupContentJson = SpecimenGroup(specimenGroupId, subjects).toJsonString()
    val coreSpecimenGroupJson = buildJsonEntity(specimenGroupContentJson, insertionDateTime, creator, componentId)

    // return a json object containing the 3 main entities
    Json.parse(s"""
      {
        "$activityLabel": $coreActivityJson,
        "$datasetLabel" : $coreDatasetJson,
        "$specimenGroupLabel": $coreSpecimenGroupJson
      }
      """).as[JsObject]
  }

  def buildJsonEntity(contentJson: String, insertionDateTime: String, creator: String, componentId: String) = {
    // hashcode is computed on spec content to avoid update because of insertionDateTime change
    // TODO consider state
    s"""
      {
        "created_timestamp": "${insertionDateTime}",
        "creator": "${creator}",
        "state": "1",
        "component_id": $componentId,
        "Specification": $contentJson
      }
      """
  }

  def getNumberOfInstancesInBlock(from: Int, to: Int, sheet: XSSFSheet): Int = {
    /*
      strongly based on:
         - key value pair template
         - instances separated by a fixed number of columns
         - first row containing block name
         - first row is full in case of multiple instances (used to compute number of instances)
         - first row contain only block title in case of single instance
     */
    val row = sheet.getRow(from)
    val rowNbChildren = Math.max(nbFilledCellsInRow(row) - 1, 1)
    rowNbChildren
  }

  def buildJsonData(from: Int, to: Int, sheet: XSSFSheet): Seq[Map[String, Seq[String]]] = {
    // detect number of entities in the block
    val nbChildren = getNumberOfInstancesInBlock(from, to, sheet)

    val jsonData = Seq.fill(nbChildren)(scala.collection.mutable.Map.empty[String, Seq[String]])
    (from + 1 to to).foreach {
      case rowIdx =>
        val row = sheet.getRow(rowIdx)
        val key = getCellContentAsString(row.getCell(columnKeyIdx))
        (firstValueIdx to firstValueIdx + (nbColumnsBetweenChildren + 1) * (nbChildren - 1) by nbColumnsBetweenChildren + 1)
          .foreach {
            case colIdx =>
              val childIdx = colIdx / 2 - 1
              val cellContent = getCellContentAsString(row.getCell(colIdx))
              val newValue: Seq[String] = jsonData(childIdx).get(key) match {
                case Some(contentSeq) =>
                  if (contentSeq.forall(_ != empty)) {
                    if (cellContent != empty) {
                      contentSeq ++ cellContent.split(cellContentSeparator)
                    } else {
                      contentSeq
                    }
                  } else {
                    cellContent.split(cellContentSeparator)
                  }
                case None =>
                  cellContent.split(cellContentSeparator)
              }
              jsonData(childIdx).put(key, newValue)
          }
    }
    jsonData.map(_.toMap)

  }

  def getContentBlocks(sheet: XSSFSheet): Seq[(String, Int, Int)] = {
    val fr = Math.max(sheet.getFirstRowNum, globalInfoLastIdx + 1)
    val lr = sheet.getLastRowNum

    (fr to lr).foldLeft(Seq((empty, -1, lr))) {
      case (curSeq, curIdx) =>
        val row = sheet.getRow(curIdx)
        val emptyRow = isEmptyRow(row)
        if (!emptyRow) {
          if (curSeq.last._2 == -1) {
            curSeq.dropRight(1) :+ (getCellContentAsString(row.getCell(sectionTitleIdx)), curIdx, lr)
          } else {
            if (curSeq.last._3 != lr) {
              curSeq :+ (getCellContentAsString(row.getCell(sectionTitleIdx)), curIdx, lr)
            } else {
              curSeq
            }
          }
        } else {
          if (curSeq.last._3 == lr) {
            curSeq.dropRight(1) :+ (curSeq.last._1, curSeq.last._2, curIdx - 1)
          } else {
            curSeq
          }
        }
    }
  }
}
