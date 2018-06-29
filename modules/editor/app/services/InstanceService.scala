
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
import common.models.NexusPath
import editor.helper.InstanceHelper._
import editor.models.{InMemoryKnowledge, Instance}
import helpers.ReconciledInstanceHelper
import models.authentication.UserInfo
import nexus.helpers.NexusHelper
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class InstanceService @Inject()(wSClient: WSClient,
                                nexusService: NexusService
                               )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)
  def retrieveIncomingLinks(nexusEndpoint:String, originalInstance: Instance,
                            token: String): Future[IndexedSeq[Instance]] = {
    val filter =
      """
        |{"op":"or","value": [{
        |   "op":"eq",
        |   "path":"https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "https://nexus-dev.humanbrainproject.org/v0/organizations/reconciled"
        | }, {
        |   "op":"eq",
        |   "path":"https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "https://nexus-dev.humanbrainproject.org/v0/organizations/manual"
        | }
        | ]
        |}
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    nexusService.listAllNexusResult(s"$nexusEndpoint/v0/data/${originalInstance.id()}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token).map {
      incomingLinks =>
        incomingLinks.map(el => Instance((el \ "source").as[JsValue])).toIndexedSeq
    }
  }

  def createReconcileInstance(nexusEndpoint:String,reconciledSpace: String, instance: JsObject, schema: String, version: String, token: String): Future[WSResponse] = {
    wSClient.url(s"$nexusEndpoint/v0/data/$reconciledSpace/${schema}/${version}").withHttpHeaders("Authorization" -> token).post(instance)
  }

  def updateReconcileInstance(nexusEndpoint:String,reconciledSpace:String, instance: JsObject, nexusPath: NexusPath, id: String, revision: Int, token: String): Future[WSResponse] = {
    wSClient.url(s"$nexusEndpoint/v0/data/$reconciledSpace/${nexusPath.schema}/${nexusPath.version}/$id?rev=${revision}").withHttpHeaders("Authorization" -> token).put(instance)
  }

  def retrieveOriginalInstance(nexusEndpoint:String, path: NexusPath, id:String, token: String): Future[Either[WSResponse, Instance]] = {
    wSClient.url(s"$nexusEndpoint/v0/data/${path.toString()}/$id?fields=all").addHttpHeaders("Authorization" -> token).get().map {
      res =>
        res.status match {
          case OK =>
            val json = res.json
            // Get data from manual space
            Right(Instance(json))
          // Get Instance through filter -> incoming link filter by space
          case _ =>
            logger.error(s"Error: Could not fetch original instance - ${res.body}")
            Left(res)
        }
    }
  }


  def upsertUpdateInManualSpace(
                                 nexusEndpoint:String,
                                 manualSpace: String,
                                 manualEntitiesDetailsOpt: Option[IndexedSeq[UpdateInfo]],
                                 userInfo: UserInfo,
                                 schema: String,
                                 manualEntity: JsObject,
                                 token: String
                               ): Future[WSResponse] = {
    manualEntitiesDetailsOpt.flatMap { manualEntitiesDetails =>
      // find manual entry corresponding to the user
      manualEntitiesDetails.filter(_._3 == userInfo.id).headOption.map {
        case (manualEntityId, manualEntityRevision, _) =>
          wSClient.url(s"$nexusEndpoint/v0/data/${Instance.getIdfromURL(manualEntityId)}/?rev=$manualEntityRevision").addHttpHeaders("Authorization" -> token).put(
            manualEntity
          )
      }
    }.getOrElse {
      wSClient.url(s"$nexusEndpoint/v0/data/$manualSpace/${schema}/v0.0.4").addHttpHeaders("Authorization" -> token).post(
        manualEntity + ("http://hbp.eu/manual#updater_id", JsString(userInfo.id))
      )
    }
  }

  def createManualSchemaIfNeeded(
                                  nexusEndpoint:String,
                                  manualEntity: JsObject,
                                  originalInstanceNexusPath: NexusPath,
                                  token: String,
                                  schemasHashMap: InMemoryKnowledge,
                                  space: String,
                                  destinationOrg: String
                                ): Future[Boolean] = {
    // ensure schema related to manual update exists or create it
    if (manualEntity != JsNull) {
      if (schemasHashMap.manualSchema.isEmpty) { // initial load
        schemasHashMap.loadManualSchemaList(token, nexusService)
      }
      if (!schemasHashMap.manualSchema.contains(s"$nexusEndpoint/v0/schemas/$space/${originalInstanceNexusPath.schema}/${originalInstanceNexusPath.version}")) {
        nexusService.createSchema(
          nexusEndpoint,
          destinationOrg,
          originalInstanceNexusPath.schema.capitalize,
          space,
          originalInstanceNexusPath.version,
          token
        ).map {
          response =>
            response.status match {
              case OK => schemasHashMap.loadManualSchemaList(token, nexusService)
                logger.info(s"Schema created properly for : " +
                  s"$space/${originalInstanceNexusPath.schema}/${originalInstanceNexusPath.version}")
                Future.successful(true)
              case _ => logger.error(s"ERROR - schema does not exist and " +
                s"automatic creation failed - ${response.body}")
                Future.successful(false)
            }
        }

      }
    }
    Future.successful(true)
  }

  def upsertReconciledInstance(
                                nexusEndpoint: String,
                                reconciledSpace:String,
                                manualSpace: String,
                                instances: IndexedSeq[Instance], originalInstance: Instance,
                                manualEntity: JsObject, updatedValue: JsObject,
                                consolidatedInstance: Instance, token: String,
                                userInfo: UserInfo,
                                inMemoryReconciledSpaceSchemas: InMemoryKnowledge
                              ): Future[WSResponse] = {

    val reconcileInstances = instances
      .filter(instance => instance.nexusPath.toString() contains reconciledSpace)
    val parentId = (originalInstance.content \ "@id").as[String]
    if (reconcileInstances.nonEmpty) {
      val reconcileInstance = reconcileInstances.head
      val parentRevision = (reconcileInstance.content \ "nxv:rev").as[Int]
      val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, Instance(updatedValue), instances, manualEntity, userInfo, parentRevision, parentId, token)
      updateReconcileInstance(nexusEndpoint, reconciledSpace, payload, reconcileInstance.nexusPath, reconcileInstance.nexusUUID, parentRevision, token)
    } else {
      val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
      val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, consolidatedInstance, instances, manualEntity, userInfo, parentRevision, parentId, token)
      createManualSchemaIfNeeded(nexusEndpoint, updatedValue, originalInstance.nexusPath, token, inMemoryReconciledSpaceSchemas, reconciledSpace, "reconciled").flatMap {
        res =>
          createReconcileInstance(
            nexusEndpoint,
            reconciledSpace,
            payload,
            consolidatedInstance.nexusPath.schema,
            consolidatedInstance.nexusPath.version,
            token
          )
      }
    }
  }

}
