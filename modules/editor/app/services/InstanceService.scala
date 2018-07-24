
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
import editor.models.{InMemoryKnowledge, IncomingLinksInstances, Instance}
import helpers.ReconciledInstanceHelper
import authentication.models.UserInfo
import nexus.helpers.NexusHelper
import play.api.{Configuration, Logger}
import play.api.http.Status.OK
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class InstanceService @Inject()(wSClient: WSClient,
                                nexusService: NexusService,
                                config: Configuration
                               )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)
  val nexusEndpoint = config.get[String]("nexus.endpoint")
  def retrieveIncomingLinks(nexusEndpoint:String, originalInstance: Instance,
                            token: String): Future[IndexedSeq[Instance]] = {
    val filter =
      s"""
        |{"op":"or","value": [{
        |   "op":"eq",
        |   "path":"$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "$nexusEndpoint/v0/organizations/reconciled"
        | }, {
        |   "op":"eq",
        |   "path":"$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "$nexusEndpoint/v0/organizations/manual"
        | }
        | ]
        |}
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    nexusService
      .listAllNexusResult(s"$nexusEndpoint/v0/data/${originalInstance.id()}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token)
      .map {
        incomingLinks =>
          incomingLinks.map(el => Instance((el \ "source").as[JsValue])).toIndexedSeq
      }
  }

  def createReconcileInstance(nexusEndpoint:String,
                              destinationOrg: String,
                              instance: JsObject,
                              domain: String,
                              schema: String,
                              version: String,
                              token: String
                             ): Future[WSResponse] = {
    wSClient
      .url(s"$nexusEndpoint/v0/data/$destinationOrg/$domain/${schema}/${version}")
      .withHttpHeaders("Authorization" -> token)
      .post(instance)
  }

  def updateReconcileInstance(nexusEndpoint:String,
                              reconciledSpace:String,
                              instance: JsObject,
                              nexusPath: NexusPath,
                              id: String,
                              revision: Int,
                              token: String
                             ): Future[WSResponse] = {
    wSClient
      .url(s"$nexusEndpoint/v0/data/$reconciledSpace/${nexusPath.schema}/${nexusPath.version}/$id?rev=${revision}")
      .withHttpHeaders("Authorization" -> token).put(instance)
  }

  def retrieveOriginalInstance(nexusEndpoint:String, path: NexusPath, id:String, token: String): Future[Either[WSResponse, Instance]] = {
    wSClient.url(s"$nexusEndpoint/v0/data/${path.toString()}/$id?fields=all&deprecated=false").addHttpHeaders("Authorization" -> token).get().map {
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
                                 destinationOrg: String,
                                 manualEntitiesDetailsOpt: Option[IndexedSeq[UpdateInfo]],
                                 userInfo: UserInfo,
                                 instancePath: NexusPath,
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
      wSClient.url(s"$nexusEndpoint/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}").addHttpHeaders("Authorization" -> token).post(
        manualEntity + ("http://hbp.eu/manual#updater_id", JsString(userInfo.id))
      )
    }
  }

  def insertNewInstance(
                     nexusEndoint: String,
                     destinationOrg: String,
                     newInstance: JsObject,
                     instancePath: NexusPath,
                     token:String
                     ): Future[WSResponse] = {
    wSClient
      .url(s"$nexusEndoint/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}")
      .addHttpHeaders("Authorization" -> token).post(newInstance)
  }

  def createManualSchemaIfNeeded(
                                  nexusEndpoint:String,
                                  manualEntity: JsObject,
                                  originalInstanceNexusPath: NexusPath,
                                  token: String,
                                  destinationOrg: String,
                                  editorOrg: String,
                                  editorContext: String = ""
                                ): Future[Boolean] = {
    // ensure schema related to manual update exists or create it
    if (manualEntity != JsNull) {
      nexusService.createSchema(
        nexusEndpoint,
        destinationOrg,
        originalInstanceNexusPath.org,
        originalInstanceNexusPath.schema.capitalize,
        originalInstanceNexusPath.domain,
        originalInstanceNexusPath.version,
        editorOrg,
        token,
        editorContext
      ).map {
        response =>
          response.status match {
            case OK =>
              logger.info(s"Schema created properly for : " +
                s"$destinationOrg/${originalInstanceNexusPath.domain}/${originalInstanceNexusPath.schema}/${originalInstanceNexusPath.version}")
              true
            case _ => logger.error(s"ERROR - schema does not exist and " +
              s"automatic creation failed - ${response.body}")
              false
          }
      }
    }else {
      Future.successful(true)
    }
  }

  def upsertReconciledInstance(
                                nexusEndpoint: String,
                                reconciledSpace:String,
                                manualSpace: String,
                                instances: IncomingLinksInstances,
                                originalInstance: Instance,
                                manualEntity: JsObject,
                                updatedValue: JsObject,
                                consolidatedInstance: Instance,
                                token: String,
                                userInfo: UserInfo
                              ): Future[WSResponse] = {

    val reconcileInstances = instances.reconciledInstances
    val parentId = (originalInstance.content \ "@id").as[String]
    if (reconcileInstances.nonEmpty) {
      val reconcileInstance = reconcileInstances.head
      val parentRevision = (reconcileInstance.content \ "nxv:rev").as[Int]
      val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, Instance(updatedValue), instances, manualEntity, userInfo, parentRevision, parentId, token)
      updateReconcileInstance(nexusEndpoint, reconciledSpace, payload, reconcileInstance.nexusPath, reconcileInstance.nexusUUID, parentRevision, token)
    } else {
      val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
      val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, consolidatedInstance, instances, manualEntity, userInfo, parentRevision, parentId, token)
      createReconcileInstance(
        nexusEndpoint,
        reconciledSpace,
        payload,
        consolidatedInstance.nexusPath.domain,
        consolidatedInstance.nexusPath.schema,
        consolidatedInstance.nexusPath.version,
        token
      )
      }
    }

}
