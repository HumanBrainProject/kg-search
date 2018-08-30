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
package data_import.services

import com.google.inject.Inject
import data_import.helpers.excel_import.ExcelInsertionHelper
import data_import.helpers.excel_import.ExcelMindsImportHelper.formatEntityPayload
import data_import.models.excel_import.CommonVars.{activityLabel, datasetLabel, specimenGroupLabel, specimengroupLabel}
import models.excel_import.{Entity, Value}
import nexus.services.NexusService
import nexus.services.NexusService._
import play.api.{Configuration, Logger}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import scala.concurrent.{ExecutionContext, Future}
import Entity.isNexusLink
import Value.DEFAULT_RESOLUTION_STATUS


class InsertionService @Inject()(wSClient: WSClient, nexusService: NexusService,config: Configuration)
                                (implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)

  def nexusResponseToStatus(nexusResponse: Future[WSResponse], operation: String): Future[Either[(String, JsValue), String]]= {
    nexusResponse.map { response =>
      response.status match {
        case 200 | 201 =>
          Left((operation, response.json))
        case _ =>
          Right(response.bodyAsBytes.utf8String)
      }
    }
  }

  def insertEntity(nexusUrl:String, org: String, domain: String, instanceType: String, version: String, payload: JsObject,
                           instanceId: String, token: String, nexusService: NexusService): Future[Either[(String, JsValue), String]] = {
    nexusResponseToStatus(
      nexusService.insertInstance(nexusUrl, org, domain, instanceType, version, payload, token),
      INSERT)
  }

  def updateEntity(instanceLink:String, rev: Option[Long], payload: JsObject, token: String, nexusService: NexusService): Future[Either[(String, JsValue), String]] = {
      nexusService.updateInstance(instanceLink, rev, payload, token).flatMap{
        case (operation, response) =>
          nexusResponseToStatus(Future.successful(response), operation)
      }
  }

  def insertOrUpdateEntity(nexusUrl:String, org: String, domain: String, instanceType: String, version: String, payload: JsObject,
                           instanceId: String, token: String, nexusService: NexusService): Future[Either[(String, JsValue), String]] = {
    val identifier = (payload \ "http://schema.org/identifier").as[String]
    nexusService.insertOrUpdateInstance(nexusUrl, org, domain, instanceType, version, payload, identifier, token).flatMap{
      case (operation, idOpt, responseOpt) =>
        responseOpt match {
          case Some(response) =>
            nexusResponseToStatus(response, operation)
          case None => // corresponds to ignore operation. Manually build JsValue to forward needed info like id
            Future.successful(Left((operation, JsObject(Map("@id" -> JsString(idOpt.getOrElse("")))))))
        }
    }
  }

  /*
  * Output: Right (url) or Left (error message)
  *
  * Logic:
  *   if _ID is a nexus link
  *     try to update linked entity
  *   else
  *     create a new entity
  *
  */
  def insertUnimindsEntity(nexusUrl:String, entity: Entity,token: String,
                           nexusService: NexusService):Future[Either[(String, JsValue), String]] = {
    val payload = entity.toJsonLd()
    entity.externalId match {
      case Some (idValue) =>
         if (isNexusLink(idValue)){
          updateEntity(idValue, None, payload, token, nexusService)
        } else {
          insertEntity(nexusUrl, "uniminds", "core", entity.`type`, "v0.0.1", payload, entity.localId, token, nexusService)
        }
      case None => // INSERT
        insertEntity(nexusUrl, "uniminds", "core", entity.`type`, "v0.0.1", payload, entity.localId, token, nexusService)
    }
  }

  def insertMindsEntity(nexusUrl: String, entityType:String, payload: JsObject, token: String): Future[Either[(String, JsValue), String]] = {
    insertOrUpdateEntity(nexusUrl, "excel", "core", entityType, "v0.0.1", payload, "", token, nexusService)
    }

  def insertMindsEntities(jsonData: JsObject, nexusEndpoint: String, token: String): Future[Seq[JsObject]] = {
    val activityPayload = formatEntityPayload((jsonData \ activityLabel).as[JsObject], activityLabel)
    val specimenGroupPayload = formatEntityPayload((jsonData \ specimenGroupLabel).as[JsObject], specimengroupLabel)

    val firstTodo = Seq((activityPayload, activityLabel.toLowerCase),
      (specimenGroupPayload, specimenGroupLabel.toLowerCase))
    // use foldleft to ensure sequential ingestion of resources and build a valid archive
    val firstResultFuture = firstTodo.foldLeft(Future.successful[Seq[JsObject]](Seq.empty[JsObject])) {
      case (futureRes, (payload, entityType)) =>
        futureRes.flatMap {
          res =>
            buildinsertionResult(payload, entityType, insertMindsEntity(nexusEndpoint, entityType, payload, token)).map{
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
          insertMindsEntity(nexusEndpoint, datasetLabel.toLowerCase, datasetPayload, token)).map{
          result =>
            firstResult :+ result
        }
    }
  }

  def retrieveEntityDetails(url: String, id: String, token: String): Future[Option[(String, Int, JsObject)]] = {
    wSClient.url(s"""$url/?deprecated=false&fields=all&filter={"op":"eq","path":"http://schema.org/identifier","value":"$id"}""")
      .addHttpHeaders(
      "Authorization" -> token)
      .get()
      .map {
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

  def buildinsertionResult(payload: JsObject, entityType: String, result: Future[Either[(String, JsValue), String]]): Future[JsObject] = {
    result.map {
      res =>
        val (statusString, linkOpt) = res match {
          case Left((status, jsonResponse)) =>
            status match {
              case SKIP => ("skipped", Some((jsonResponse \ "@id").as[String]))
              case UPDATE => ("updated", Some((jsonResponse \ "@id").as[String]))
              case INSERT => ("inserted", Some((jsonResponse \ "@id").as[String]))
              case ERROR => (s"failed", None)
            }
          case Right(errorMessage) =>
            ("failed", None)
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

  def insertUnimindsDataInKG(nexusEndPoint: String, data: Seq[Entity], token: String, nexusService: NexusService,
                             insertionService: InsertionService): Future[Seq[Entity]] = {

    val dataRef = data.map(e => (e.localId, e)).toMap
    val insertSeq = ExcelInsertionHelper.buildInsertableEntitySeq(dataRef)

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
    insertSeq.foldLeft(Future.successful(Seq.empty[Entity])) {
      case (statusSeqFut, entity) =>
        statusSeqFut.flatMap { statusSeq =>
          val resolvedEntity = entity.resolveLinks(linksRef)
          insertUnimindsEntity(nexusEndPoint, resolvedEntity, token, nexusService).flatMap {
            insertionResponse => insertionResponse match {
              case Left((operation, jsonResponse)) =>
                val instanceLink = (jsonResponse \ "@id").as[String]
                linksRef.put(entity.localId, instanceLink)
                val status = operation match {
                  case SKIP => s"NO CHANGE"
                  case INSERT | UPDATE => s"${operation} OK"
                  case _ => DEFAULT_RESOLUTION_STATUS
                }
                logger.info(s"[uniminds][insertion][${status}] ${resolvedEntity.`type`}.${resolvedEntity.localId}")
                val updatedEntity = resolvedEntity.validateLinksAndStatus(Some(instanceLink), Some(status), token, nexusService)
                updatedEntity.map{
                  logger.info(s"[uniminds][validation][DONE]${resolvedEntity.`type`}.${resolvedEntity.localId}")
                  statusSeq :+ _
                }

              case Right(insertionError) =>
                val errorMsg = try {
                  (Json.parse(insertionError).as[JsObject] \ "code").as[String]
                } catch {
                  case _:Throwable  => insertionError
                }
                logger.info(s"[uniminds][insertion][${errorMsg}] ${resolvedEntity.`type`}.${resolvedEntity.localId}")
                val updatedEntity = resolvedEntity.validateLinksAndStatus(None, Some(s"${ERROR}: $errorMsg"), token, nexusService)
                updatedEntity.map{
                  logger.info(s"[uniminds][validation][DONE]${resolvedEntity.`type`}.${resolvedEntity.localId}")
                  statusSeq :+ _
                }
            }
          }
        }
    }
  }
}
