
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

import java.security.MessageDigest
import java.util.Calendar

import data_import.models.excel_import.CommonVars._
import data_import.models.excel_import._
import org.apache.poi.ss.usermodel.{Cell, CellType, DataFormatter}
import org.apache.poi.xssf.usermodel._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}


object ExcelImportHelper {

  val actionPreview = "preview"
  val actionInsert = "insert"
  val dataFormatter = new DataFormatter()
  var formulaEvaluator = new XSSFWorkbook().getCreationHelper().createFormulaEvaluator()

  def hash(payload: String): String = {
    MessageDigest.getInstance("MD5").digest(payload.getBytes).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {_ + _}
  }

  def retrieveEntityDetails(url: String, id: String, token: String)(implicit wSClient: WSClient, executionContext: ExecutionContext): Future[Option[(String, Int, JsObject)]] = {
    wSClient.url(s"""$url/?deprecated=false&fields=all&filter={"op":"eq","path":"http://schema.org/identifier","value":"$id"}""").addHttpHeaders(
      "Authorization" -> token
    ).get().map {
      result =>
        val content = result.json.as[JsObject]
        val total = (content \ "total").as[Int]
        if (total == 0) {
          None
        } else {
          val firstResult = (content \ "results").as[JsArray].value.head.as[JsObject]
          Some(((firstResult \ "resultId").as[String], (firstResult \ "source" \ "nxv:rev").as[Int], (firstResult \ "source").as[JsObject] - "links" - "@id" - "nxv:rev" - "nxv:deprecated"))
        }
    }
  }

  def buildinsertionResult(payload: JsObject, entityType: String, result: Future[(Int, Option[JsObject])])
                          (implicit wSClient: WSClient, executionContext: ExecutionContext): Future[JsObject] = {
    result.map {
      case (status, bodyOpt) =>
        val (statusString, linkOpt) = status match {
          case -1 => ("skipped", Some((bodyOpt.get \ "@id").as[String]))
          case 200 => ("updated", Some((bodyOpt.get \ "@id").as[String]))
          case 201 => ("inserted", Some((bodyOpt.get \ "@id").as[String]))
          case _ => ("failed", None)
        }
        val entityId = (payload \ "http://schema.org/identifier").as[String]
        val linkString = linkOpt match {
          case Some(link) => s""" "link": "$link", """
          case None => ""
        }
        Json.parse(
          s"""
          {
            $linkString
            "id":  "$entityId",
            "type": "$entityType",
            "status": "$statusString"
          }
          """).as[JsObject]
    }
  }

  def insertEntity(payload: JsObject, nexusEndpoint: String, entityCompleteType:String, token: String)
                  (implicit wSClient: WSClient, executionContext: ExecutionContext): Future[(Int, Option[JsObject])] = {
    val payloadId = (payload \ "http://schema.org/identifier").as[String]

    val currentContentHash = hash(payload.toString())
    // check if it's an insert or update
    retrieveEntityDetails(s"$nexusEndpoint/v0/data/$entityCompleteType", payloadId, token).flatMap {
      entityDetails =>
        entityDetails match {
          case Some((entityLink, revNumber, previousContent)) =>
            val previousContentHash = hash(previousContent.toString())
            if (previousContentHash != currentContentHash) {
              wSClient.url(s"${entityLink}?rev=${revNumber}").addHttpHeaders("Authorization" -> token).put(payload).map{
                result=>
                  (result.status, Some(result.json.as[JsObject]))
              }
            } else {
              Future(-1, Some(JsObject(Seq(("@id", JsString(entityLink))))))
            }
          case None =>
            wSClient.url(s"${nexusEndpoint}/v0/data/$entityCompleteType")
              .addHttpHeaders("Authorization" -> token).post(payload).map{
              result =>
                (result.status, Some(result.json.as[JsObject]))
            }
        }
    }
  }

  def nbFilledCellsInRow(row: XSSFRow): Int = {
    var filledCell = 0
    val it = row.cellIterator()
    while(it.hasNext){
      if (getCellContentAsString(it.next.asInstanceOf[XSSFCell]) != empty) filledCell +=1
    }
    filledCell
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

  def isEmptyRow(row: XSSFRow): Boolean = {
    val it = row.cellIterator()
    while (it.hasNext()){
      if (getCellContentAsString(it.next().asInstanceOf[XSSFCell]) != empty) {
        return false
      }
    }
    true
  }


}