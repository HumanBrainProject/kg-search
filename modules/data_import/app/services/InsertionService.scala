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
package services

import com.google.inject.Inject
import constants.SchemaFieldsConstants
import helpers.excel.ExcelInsertionHelper
import helpers.excel.ExcelMindsImportHelper._
import models.NexusPath
import models.excel.CommonVars._
import models.excel.Entity
import models.excel.Entity.isNexusLink
import models.excel.Value.DEFAULT_RESOLUTION_STATUS
import models.dataimportstatus.{DataImportStatus, ErrorStatus, SuccessStatus}
import monix.eval.Task
import play.api.Logger
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import services.NexusService._

import scala.util.Try

class InsertionService @Inject()(wSClient: WSClient, nexusService: NexusService) {

  val logger = Logger(this.getClass)

  def nexusResponseToStatus(
    nexusResponse: Task[WSResponse],
    operation: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = {
    nexusResponse.map { response =>
      response.status match {
        case OK | CREATED =>
          Right((SuccessStatus(label = operation), response.json))
        case _ =>
          logger.debug(response.body)
          val errorMsg = Try {
            (Json.parse(response.body).as[JsObject] \ "code").as[String]
          }.toOption match {
            case Some(code) => ErrorStatus(label = code, message = response.body)
            case None       => ErrorStatus(label = operation, message = response.bodyAsBytes.utf8String)
          }
          Left(errorMsg)
      }
    }
  }

  def insertEntity(
    nexusUrl: String,
    nexusPath: Option[NexusPath],
    payload: JsObject,
    instanceId: String,
    token: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = nexusPath match {
    case Some(path) =>
      nexusResponseToStatus(nexusService.insertInstance(nexusUrl, path, payload, token), INSERT)
    case None => Task.pure(Left(ErrorStatus(label = "Invalid entity path", message = "")))
  }

  def updateEntity(
    instanceLink: String,
    rev: Option[Long],
    payload: JsObject,
    token: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = {
    nexusService.updateInstance(instanceLink, rev, payload, token).flatMap {
      case (operation, response) =>
        nexusResponseToStatus(Task.pure(response), operation)
    }
  }

  def insertOrUpdateEntity(
    nexusUrl: String,
    nexusPath: NexusPath,
    payload: JsObject,
    instanceId: String,
    token: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = {
    val identifier = (payload \ SchemaFieldsConstants.IDENTIFIER).as[String]
    nexusService.insertOrUpdateInstance(nexusUrl, nexusPath, payload, identifier, token).flatMap {
      case (operation, idOpt, responseOpt) =>
        responseOpt match {
          case Some(response) =>
            nexusResponseToStatus(response, operation)
          case None => // corresponds to ignore operation. Manually build JsValue to forward needed info like id
            Task.pure(Right((SuccessStatus(label = operation), JsObject(Map("@id" -> JsString(idOpt.getOrElse("")))))))
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
  def insertUnimindsEntity(
    nexusUrl: String,
    entity: Entity,
    token: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = {
    val payload = entity.toJsonLd()
    entity.externalId match {
      case Some(idValue) =>
        if (isNexusLink(idValue)) {
          updateEntity(idValue, None, payload, token)
        } else {
          insertEntity(nexusUrl, entity.path, payload, entity.localId, token)
        }
      case None => // INSERT
        insertEntity(nexusUrl, entity.path, payload, entity.localId, token)
    }
  }

  def insertMindsEntity(
    nexusUrl: String,
    entityType: String,
    payload: JsObject,
    token: String
  ): Task[Either[ErrorStatus, (SuccessStatus, JsValue)]] = {
    val path = NexusPath("excel", "core", entityType, "v0.0.1")
    insertOrUpdateEntity(nexusUrl, path, payload, "", token)
  }

  def insertMindsEntities(jsonData: JsObject, nexusEndpoint: String, token: String): Task[Seq[JsObject]] = {
    val activityPayload = formatEntityPayload((jsonData \ activityLabel).as[JsObject], activityLabel)
    val specimenGroupPayload = formatEntityPayload((jsonData \ specimenGroupLabel).as[JsObject], specimengroupLabel)

    val firstTodo =
      Seq((activityPayload, activityLabel.toLowerCase), (specimenGroupPayload, specimenGroupLabel.toLowerCase))
    // use foldleft to ensure sequential ingestion of resources and build a valid archive
    val firstResultFuture = firstTodo.foldLeft(Task[Seq[JsObject]](Seq.empty[JsObject])) {
      case (futureRes, (payload, entityType)) =>
        futureRes.flatMap { res =>
          buildinsertionResult(payload, entityType, insertMindsEntity(nexusEndpoint, entityType, payload, token)).map {
            result =>
              res :+ result
          }
        }
    }
    firstResultFuture.flatMap { firstResult =>
      val parentLinks = firstResult.flatMap { res =>
        if (res.keys.contains("link")) {
          Some(JsObject(Seq(("@id", (res \ "link").as[JsString]))))
        } else {
          None
        }
      }
      val parentBlock = if (parentLinks.nonEmpty) Some(JsArray(parentLinks)) else None
      val datasetPayload = formatEntityPayload((jsonData \ datasetLabel).as[JsObject], datasetLabel, parentBlock)
      buildinsertionResult(
        datasetPayload,
        datasetLabel.toLowerCase,
        insertMindsEntity(nexusEndpoint, datasetLabel.toLowerCase, datasetPayload, token)
      ).map { result =>
        firstResult :+ result
      }
    }
  }

  def retrieveEntityDetails(url: String, id: String, token: String): Task[Option[(String, Int, JsObject)]] = {
    Task
      .deferFuture(
        wSClient
          .url(
            s"""$url/?deprecated=false&fields=all&filter={"op":"eq","path":"${SchemaFieldsConstants.IDENTIFIER}","value":"$id"}"""
          )
          .addHttpHeaders("Authorization" -> token)
          .get()
      )
      .map { result =>
        val content = result.json.as[JsObject]
        val total = (content \ "total").as[Int]
        if (total == 0) {
          None
        } else {
          val firstResult = (content \ "results").as[JsArray].value.head.as[JsObject]
          Some(
            (
              (firstResult \ "resultId").as[String],
              (firstResult \ "source" \ "nxv:rev").as[Int],
              (firstResult \ "source").as[JsObject] - "links" - "@id" - "nxv:rev" - "nxv:deprecated"
            )
          )
        }
      }
  }

  def buildinsertionResult(
    payload: JsObject,
    entityType: String,
    result: Task[Either[ErrorStatus, (SuccessStatus, JsValue)]]
  ): Task[JsObject] = {
    result.map { res =>
      val (statusString, linkOpt) = res match {
        case Right((status, jsonResponse)) =>
          status.label match {
            case SKIP   => ("skipped", Some((jsonResponse \ "@id").as[String]))
            case UPDATE => ("updated", Some((jsonResponse \ "@id").as[String]))
            case INSERT => ("inserted", Some((jsonResponse \ "@id").as[String]))
            case ERROR  => (s"failed", None)
          }
        case Left(_) =>
          ("failed", None)
      }
      val entityId = (payload \ SchemaFieldsConstants.IDENTIFIER).as[String]
      val linkString = linkOpt match {
        case Some(link) => s""" "link": "$link", """
        case None       => ""
      }
      Json.parse(s"""
          {
            $linkString
            "id":  "$entityId",
            "type": "$entityType",
            "status": "$statusString"
          }
          """).as[JsObject]
    }
  }

  /**
    *  Create schema if it does not exists and publish it. The domain will also be created if needed
    * @param paths All the paths of the entities the could contain non existant schemas
    * @param nexusEndPoint the base url of the DB
    * @param token the user token
    * @return A list of schema to insert
    */
  private def createSchemas(paths: Seq[NexusPath], nexusEndPoint: String, token: String): Seq[Task[String]] = {
    paths.foldLeft(Seq[Task[String]]()) {
      case (l, path) =>
        val task =
          nexusService.createDomain(nexusEndPoint, path.org, path.domain, "Domain for ${path.domain}", token).flatMap {
            _ =>
              nexusService.createSimpleSchema(nexusEndPoint, path, token).map { response =>
                logger.debug(s"${response.status}: ${response.body}")
                s"${response.status}: ${response.body}"
              }
          }
        l :+ task
    }
  }

  /**
    *
    * @param insertSeq
    * @param nexusEndPoint
    * @param token
    * @return
    */
  private def insertEntities(
    insertSeq: Seq[Entity],
    nexusEndPoint: String,
    token: String
  ): Task[Seq[Entity]] = {
    val linksRef = collection.mutable.Map.empty[String, String]
    insertSeq.foldLeft(Task.pure(Seq.empty[Entity])) {
      case (statusSeqFut, entity) =>
        statusSeqFut.flatMap { statusSeq =>
          val resolvedEntity = entity.resolveLinks(linksRef)
          insertUnimindsEntity(nexusEndPoint, resolvedEntity, token).flatMap {
            case Right((operation, jsonResponse)) =>
              val instanceLink = (jsonResponse \ "@id").as[String]
              linksRef.put(entity.localId, instanceLink)
              validateLinksAndStatus(operation, resolvedEntity, statusSeq, Some(instanceLink), token)
            case Left(insertionError) =>
              validateLinksAndStatus(insertionError, resolvedEntity, statusSeq, None, token)

          }
        }
    }
  }

  private def validateLinksAndStatus(
    status: DataImportStatus,
    resolvedEntity: Entity,
    statusSeq: Seq[Entity],
    instanceLink: Option[String],
    token: String
  ) = {
    val newStatus = status match {
      case SuccessStatus(operation, _) =>
        val statusStr = operation match {
          case SKIP            => s"NO CHANGE"
          case INSERT | UPDATE => s"${operation} OK"
          case _               => DEFAULT_RESOLUTION_STATUS
        }
        logger.info(s"[uniminds][insertion][${statusStr}] ${resolvedEntity.`type`}.${resolvedEntity.localId}")
        statusStr
      case ErrorStatus(_, message) =>
        s"${ERROR}: $message"
    }
    val updatedEntity = resolvedEntity.validateLinksAndStatus(instanceLink, Some(newStatus), token, nexusService)
    updatedEntity.map {
      logger.info(s"[uniminds][validation][DONE]${resolvedEntity.`type`}.${resolvedEntity.localId}")
      statusSeq :+ _
    }
  }

  /**
    *  Generating a graph structure of local entities and insterting them in order from leaf to root
    * @param nexusEndPoint base url for insersion
    * @param data the list of extracted entities
    * @param token the user token
    * @return a task describing the insertion of the entities in the DB
    */
  def insertUnimindsDataInKG(nexusEndPoint: String, data: Seq[Entity], token: String): Task[Seq[Entity]] = {

    val dataRef = data.map(e => (e.localId, e)).toMap
    val insertSeq = ExcelInsertionHelper.buildInsertableEntitySeq(dataRef)

    // create schemas if needed
    val paths = data.collect { case Entity(_, _, _, Some(path), _) => path }.distinct
    val createdSchemas = createSchemas(paths, nexusEndPoint, token)
    Task.gather(createdSchemas).flatMap { _ =>
      // insert entities
      insertEntities(insertSeq, nexusEndPoint, token)
    }

  }
}
