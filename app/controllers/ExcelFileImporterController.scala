package controllers

import java.io.FileInputStream
import java.util.Calendar

import javax.inject.{Inject, Singleton}
import models._
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.CellType
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import org.apache.poi.xssf.usermodel._
import play.api.libs.json._
import play.api.libs.json
import ExcelFileImporterController._
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.stream.StreamRefMessages.Payload
import org.apache.poi.ss.usermodel.DataFormatter

@Singleton
class ExcelFileImporterController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
  extends AbstractController(cc) {
  val logger = Logger(this.getClass)
  val nexusEndpoint = config.get[String]("nexus.endpoint")

  def extractDataFromExcel(action: Option[String]) = Action.async(parse.temporaryFile) { request =>
    val path = request.body.path
    val fis = new FileInputStream(path.toFile)

    val wb = new XSSFWorkbook(fis)
    ExcelFileImporterController.formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator()
    val jsonData = buildJsonMindsDataFromExcel(wb)

    action.getOrElse(actionPreview) match {
      case `actionInsert` =>
        val tokenOpt = request.headers.toSimpleMap.get("Authorization")
        tokenOpt match {
          case Some(token) =>
            insertEntities(jsonData, nexusEndpoint, token).map {
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

}

object ExcelFileImporterController {

  import models.CommonVars._
  import java.security.MessageDigest

  val actionPreview = "preview"
  val actionInsert = "insert"
  val dataFormatter = new DataFormatter()
  var formulaEvaluator = new XSSFWorkbook().getCreationHelper().createFormulaEvaluator()

  def hash(payload: String): String = {
    MessageDigest.getInstance("MD5").digest(payload.getBytes).toString
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

 def formatEntityPayload(payload: JsObject, entityType: String, parent: Option[JsValue] = None): JsObject = {
   val payloadId = (payload \ "Specification" \ valueLabel).as[String]
   val mainContent = Seq(
     ("@type", JsString(s"http://hbp.eu/dw#$entityType")),
     ("http://schema.org/identifier" -> JsString(payloadId)),
     ("http://hbp.eu/dw#raw_content" -> payload))
   val fullContent = parent match {
     case Some(parentObj) => mainContent :+ ("http://hbp.eu/dw#isLinkedWith", parentObj)
     case None => mainContent
   }
   JsObject(fullContent)
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
        val linkSting = linkOpt match {
          case Some(link) => s""" "link": "$link", """
          case None => ""
        }
        Json.parse(
          s"""
          {
            $linkSting
            "id":  "$entityId",
            "type": "$entityType",
            "status": "$statusString"
          }
          """).as[JsObject]
    }
  }

  def insertEntities(jsonData: JsObject, nexusEndpoint: String, token: String)
                    (implicit wSClient: WSClient, executionContext: ExecutionContext): Future[Seq[JsObject]] = {
    val activityPayload = formatEntityPayload((jsonData \ activityLabel).as[JsObject], activityLabel)
    val specimenGroupPayload = formatEntityPayload((jsonData \ specimenGroupLabel).as[JsObject], specimengroupLabel)

    val firstTodo = Seq((activityPayload, activityLabel.toLowerCase),
                   (specimenGroupPayload, specimenGroupLabel.toLowerCase))
    // use foldleft to ensure sequential ingestion of resources and build a valid archive
    val firstResultFuture = firstTodo.foldLeft(Future.successful[Seq[JsObject]](Seq.empty[JsObject])) {
      case (futureRes, (payload, entityType)) =>
        futureRes.flatMap {
          res =>
            buildinsertionResult(payload, entityType, insertEntity(payload, nexusEndpoint, entityType, token)).map{
              result =>
                res :+ result
            }
        }
    }
    firstResultFuture.flatMap{
      firstResult =>
        val parentLinks = firstResult.flatMap{
          res =>
            if (res.keys.contains("link")){
              Some(JsObject(Seq(
                ("@id", (res \ "link").as[JsString]))))
            } else {
              None
            }
        }
        val parentBlock = if (parentLinks.nonEmpty) Some(JsArray(parentLinks)) else None
        val datasetPayload = formatEntityPayload((jsonData \ datasetLabel).as[JsObject], datasetLabel, parentBlock)
        buildinsertionResult(datasetPayload, datasetLabel.toLowerCase,
          insertEntity(datasetPayload, nexusEndpoint, datasetLabel.toLowerCase, token)).map{
          result =>
            firstResult :+ result
        }
    }
  }

  def insertEntity(payload: JsObject, nexusEndpoint: String, entityType:String, token: String)
                  (implicit wSClient: WSClient, executionContext: ExecutionContext): Future[(Int, Option[JsObject])] = {
    val payloadId = (payload \ "http://schema.org/identifier").as[String]

    val currentContentHash = hash(payload.toString())
    // check if it's an insert or update
    retrieveEntityDetails(s"$nexusEndpoint/v0/data/dataworkbench/core/$entityType/v0.0.1", payloadId, token).flatMap {
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
            wSClient.url(s"${nexusEndpoint}/v0/data/dataworkbench/core/$entityType/v0.0.1")
              .addHttpHeaders("Authorization" -> token).post(payload).map{
              result =>
                (result.status, Some(result.json.as[JsObject]))
            }
        }
    }
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
    val samplesGroups = res(sampleSectionLabel).map {
      case sampleDetails =>
        Sample(sampleDetails(sampleIdLabel).head, sampleDetails)
    }.groupBy(_.details(subjectIdLabel).head)

    val subjects = res(specimenGroupSectionLabel).map{
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

  def nbFilledCellsInRow(row: XSSFRow): Int = {
    var filledCell = 0
    val it = row.cellIterator()
    while(it.hasNext){
      if (getCellContentAsString(it.next.asInstanceOf[XSSFCell]) != empty) filledCell +=1
    }
    filledCell
  }

  def getNumberOfInstancesInBlock(from: Int, to:Int, sheet: XSSFSheet): Int = {
    /*
      strongly based on:
         - key value pair template
         - instances are separated by a fixed number of columns
         - first row containing block name
         - first row is full in case of multiple instances (used to compute number of instances)
         - first row contain only block title in case of single instance
    */
    val row = sheet.getRow(from)
    val rowNbChildren = Math.max(nbFilledCellsInRow(row) -1, 1)
    rowNbChildren
  }

  def buildJsonData(from: Int, to: Int, sheet: XSSFSheet): Seq[Map[String, Seq[String]]] = {
    // detect number of entities in the block
    val nbChildren = getNumberOfInstancesInBlock(from, to, sheet)

    val jsonData = Seq.fill(nbChildren)(scala.collection.mutable.Map.empty[String,Seq[String]])
    (from+1 to to).foreach{
      case rowIdx =>
        val row = sheet.getRow(rowIdx)
        val key = getCellContentAsString(row.getCell(columnKeyIdx))
        (firstValueIdx to firstValueIdx + (nbColumnsBetweenChildren+1)*(nbChildren-1) by nbColumnsBetweenChildren+1).foreach{
          case colIdx =>
            val childIdx = colIdx/2 - 1
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

  def getCellContentAsString(cell: XSSFCell): String = {
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
      if (getCellContentAsString(it.next().asInstanceOf[XSSFCell]) != empty)
        return false
    }
    return true
  }

  def getContentBlocks(sheet: XSSFSheet): Seq[(String, Int, Int)] = {
    val fr = Math.max(sheet.getFirstRowNum, globalInfoLastIdx +1)
    val lr = sheet.getLastRowNum

    (fr to lr).foldLeft(Seq((empty, -1, lr))) {
      case (curSeq, curIdx) =>
        val row = sheet.getRow(curIdx)
        val emptyRow = isEmptyRow(row)
        if (!emptyRow) {
          if (curSeq.last._2 == -1) {
            curSeq.dropRight(1) :+ (getCellContentAsString(row.getCell(sectionTitleIdx)), curIdx, lr)
          } else{
            if (curSeq.last._3 != lr){
              curSeq :+ (getCellContentAsString(row.getCell(sectionTitleIdx)), curIdx, lr)
            } else{
              curSeq
            }
          }
        } else {
          if (curSeq.last._3 == lr) {
            curSeq.dropRight(1) :+ (curSeq.last._1, curSeq.last._2, curIdx - 1)
          } else{
            curSeq
          }
        }
    }
  }

}


