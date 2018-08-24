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
package data_import.controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import data_import.helpers.excel_import.{ExcelImportHelper, ExcelMindsImportHelper, ExcelUnimindsImportHelper}
import data_import.helpers.excel.ExcelUnimindsExportHelper
import data_import.services.InsertionService
import java.io.{ByteArrayOutputStream, FileInputStream}
import javax.inject.{Inject, Singleton}
import models.excel_import.Entity
import nexus.services.NexusService
import org.apache.poi.xssf.usermodel._
import play.api.http.HttpEntity
import play.api.libs.Files
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import scala.collection.immutable.HashSet
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ExcelImportController @Inject()(cc: ControllerComponents,
                                      insertionService: InsertionService,
                                      nexusService: NexusService)
                                     (implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
  extends AbstractController(cc) {

  val xlsxMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
  val xlsMime = "application/vnd.ms-excel"
  val csvMime = "text/csv"
  val AcceptsXlsx = Accepting(xlsxMime)
  val AcceptsXls = Accepting(xlsMime)
  val AcceptsCsv = Accepting(csvMime)

  val logger = Logger(this.getClass)
  val nexusEndpoint = config.get[String]("nexus.endpoint")


  def extractMindsDataFromExcel(action: Option[String]) = Action.async(parse.temporaryFile) { request =>
    val path = request.body.path
    val fis = new FileInputStream(path.toFile)

    val wb = new XSSFWorkbook(fis)
    ExcelImportHelper.formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator()
    val jsonData = ExcelMindsImportHelper.buildJsonMindsDataFromExcel(wb)

    action.getOrElse(ExcelImportHelper.actionPreview) match {
      case ExcelImportHelper.actionInsert =>
        val tokenOpt = request.headers.toSimpleMap.get("Authorization")
        tokenOpt match {
          case Some(token) =>
            insertionService.insertMindsEntities(jsonData, nexusEndpoint, token).map {
              res =>
                Ok(JsObject(Seq(("insertion result", JsArray(res)))))
            }
          case None =>
            Future.successful(Ok(
              Json.parse("{\"error\": \"You're not allowed to write in KG dataworkbench space. Please check your access token\"}")
                .as[JsObject]))
        }
      case _ =>
        // insert elements SpecimenGroup, Activity and Dataset from jsonData
        Future.successful(Ok(jsonData))
    }
  }


  def extractUnimindsDataFromExcel(action: Option[String]) = Action.async(parse.multipartFormData) { implicit request =>
    action.getOrElse(ExcelImportHelper.actionPreview) match {
      case ExcelImportHelper.actionInsert =>
        val tokenOpt = request.headers.toSimpleMap.get("Authorization")
        tokenOpt match {
          case Some(token) =>
            val dataOpt = extractDataFromRequestInput()
            dataOpt match {
              case Some((filename, data)) =>
                handleInsertRequest(filename, data, token)
              case None =>
                Future.successful(Ok(
                  "ERROR - please provide a file using mulitpart-form with inputFile as key"))
            }
          case None =>
            Future.successful(Ok(
              "ERROR - You're not allowed to write in KG uniminds space. Please check your access token or contact KG admins"))
        }
      case _ => // preview output
        extractDataFromRequestInput() match {
          case Some((fileName, data)) =>
            handlePreviewRequest(fileName, data)
          case None =>
            Future.successful(Ok(
              "ERROR - please provide a file using mulitpart-form with inputFile as key"))
        }
    }
  }

  def extractDataFromRequestInput()
                                 (implicit request: Request[MultipartFormData[Files.TemporaryFile]]): Option[(String, Seq[Entity])] = {

    request.body.file("inputFile").map {
      inputFile =>
        val inputFileName = inputFile.filename
        val path = inputFile.ref.path
        val fis = new FileInputStream(path.toFile)

        val workbook = new XSSFWorkbook(fis)
        ExcelImportHelper.formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator()
        val unimindsSheet = workbook.getSheetAt(0)
        val data = ExcelUnimindsImportHelper.extractCoreData(unimindsSheet)

        (inputFileName, data)
    }
  }

  def renderCsv(outputFileBaseName: String, data: Seq[Entity]): Result = {
    val start = System.currentTimeMillis()
    val csvContent =(
      Seq("block name^block id^key^value^unit of value^resolution status\n") ++
        data.flatMap(_.toCsv())
      ).map(line => ByteString(line.getBytes()))
    logger.info(s"[uniminds][csv] Generated in ${System.currentTimeMillis() - start} ms")

    Result(
      header = ResponseHeader(
        OK,
        Map("Content-Disposition" -> s"attachment; filename=${outputFileBaseName}.csv; filename*=utf-8''${outputFileBaseName}.csv")),
      body = HttpEntity.Streamed(Source[ByteString](collection.immutable.Iterable(csvContent:_*)),
        Some(csvContent.size.toLong), Some("text/csv"))
    )
  }

  def renderExcel(outputFileBaseName: String, data: Seq[Entity]): Result = {
    val start = System.currentTimeMillis()
    val workbook = ExcelUnimindsExportHelper.buildExcelFromEntities(data)
    logger.info(s"[uniminds][excel] Generated in ${System.currentTimeMillis() - start} ms")
    val byteArrayOutputStream = new ByteArrayOutputStream()
    workbook.write(byteArrayOutputStream)
    val byteString = ByteString(byteArrayOutputStream.toByteArray)
    Result(
      header = ResponseHeader(
        OK,
        Map("Content-Disposition" -> s"attachment; filename=${outputFileBaseName}.xlsx; filename*=utf-8''${outputFileBaseName}.xlsx")),
      body = HttpEntity.Streamed(Source[ByteString](collection.immutable.Iterable(byteString)),
        Some(byteString.size.toLong), Some(xlsxMime))
    )
  }

  def renderJson(data: Seq[Entity]): Result = {
    Ok(JsArray(
        data.zipWithIndex.sortWith{
          case ((e1, _), (e2, _)) => s"${e1.`type`}_${e1.localId}" < s"${e2.`type`}_${e2.localId}"}
        .map {
          case (entity, idx) =>
            entity.toJson() +
              ("insertion_seqNum" -> JsString(idx.toString))
        })
    )
  }

  def handleInsertRequest(inputFilename: String, data: Seq[Entity], token: String)
                         (implicit request: Request[MultipartFormData[Files.TemporaryFile]]): Future[Result] = {
    val outputFileBaseName = inputFilename.substring(0, inputFilename.indexOf('.'))
    insertionService.insertUnimindsDataInKG(nexusEndpoint, data, token, nexusService, insertionService).map{
      res =>
        // sort output
        val data = res.sortWith{
          case (e1, e2) =>
            s"${e1.`type`}_${e1.localId}" < s"${e2.`type`}_${e2.localId}"
        }
        render {
          case AcceptsXlsx() | AcceptsXls() =>
            renderExcel(outputFileBaseName, data)
          case AcceptsCsv() =>
            renderCsv(outputFileBaseName, data)
          case _ =>
            renderJson(res)
        }
    }
  }

  def handlePreviewRequest(fileName: String, data: Seq[Entity])
                          (implicit request: Request[MultipartFormData[Files.TemporaryFile]]): Future[Result] = {
    val outputFileBaseName = fileName.substring(0, fileName.indexOf('.'))
    val refData = HashSet(data.map(_.localId):_*)
    val previewValidatedData = data.map(e => e.checkInternalLinksValidity(refData))
    Future.successful(
      render {
        case AcceptsXlsx() | AcceptsXls() =>
          renderExcel(outputFileBaseName, previewValidatedData)
        case AcceptsCsv() =>
          renderCsv(outputFileBaseName, previewValidatedData)
        case _ =>
          renderJson(previewValidatedData)
      }
    )
  }
}
