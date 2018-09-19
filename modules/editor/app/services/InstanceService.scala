
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

package editor.services

import java.net.URLEncoder
import java.security.Policy.Parameters

import com.google.inject.Inject
import common.models.{NexusInstance, NexusPath, User}
import editor.helper.InstanceHelper._
import editor.models.{InMemoryKnowledge, IncomingLinksInstances}
import helpers.ReconciledInstanceHelper
import editor.controllers.NexusEditorController
import editor.helper.InstanceHelper
import editor.helpers.{FormHelper, NavigationHelper}
import nexus.helpers.NexusHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class InstanceService @Inject()(wSClient: WSClient,
                                nexusService: NexusService,
                                config: Configuration
                               )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)
  val nexusEndpoint = config.get[String]("nexus.endpoint")
  val reconciledPrefix: String = config.getOptional[String]("nexus.reconciled.prefix").getOrElse("reconciled")
  val editorPrefix: String = config.getOptional[String]("nexus.editor.prefix").getOrElse("editor")

  def retrieveIncomingLinks(
                             originalId: String,
                             originalOrg: String,
                             token: String
                           ): Future[IndexedSeq[NexusInstance]] = {
    val filter =
      s"""
         |{"op":"or","value": [{
         |   "op":"eq",
         |   "path":"$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/organization",
         |   "value": "$nexusEndpoint/v0/organizations/${originalOrg}${reconciledPrefix}"
         | }, {
         |   "op":"eq",
         |   "path":"$nexusEndpoint/vocabs/nexus/core/terms/v0.1.0/organization",
         |   "value": "$nexusEndpoint/v0/organizations/${originalOrg}${editorPrefix}"
         | }
         | ]
         |}
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    nexusService
      .listAllNexusResult(s"$nexusEndpoint/v0/data/${originalId}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token)
      .map {
        incomingLinks =>
          incomingLinks.map(el => NexusInstance((el \ "source").as[JsValue])).toIndexedSeq
      }
  }

  def insertInstance(
                      destinationOrg: String,
                      newInstance: JsObject,
                      instancePath: NexusPath,
                      token: String
                    ): Future[WSResponse] = {
    wSClient
      .url(s"$nexusEndpoint/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}")
      .addHttpHeaders("Authorization" -> token).post(newInstance)
  }

  def updateReconcileInstance(
                               instance: JsObject,
                               nexusPath: NexusPath,
                               id: String,
                               revision: Int,
                               token: String
                             ): Future[WSResponse] = {
    wSClient
      .url(s"$nexusEndpoint/v0/data/${nexusPath.toString()}/$id?rev=${revision}")
      .withHttpHeaders("Authorization" -> token).put(instance)
  }


  /**
    * Return a instance by its nexus ID
    * Starting by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    * @param path
    * @param id The id of the instance
    * @param token The user access token
    * @return An error response or an the instance
    */
  def retrieveInstance(path: NexusPath, id: String, token: String, parameters: List[(String, String)] = List()): Future[Either[WSResponse, NexusInstance]] = {
    val reconciledPath = path.reconciledPath(reconciledPrefix)
    getInstance(reconciledPath, id, token, parameters).flatMap[Either[WSResponse, NexusInstance]] {
      case Left(_) => // Check in the original space
        retrieveReconciledFromOriginal(path, reconciledPath.org, id, token, parameters).flatMap[Either[WSResponse, NexusInstance]] {
          case Left(r) =>
            Future.successful(Left(r))
          case Right(reconciledInstance) =>
            if (reconciledInstance.isDefined) {
              Future.successful(Right(reconciledInstance.get))
            } else {
              getInstance(path, id, token, parameters)
            }
        }
      case Right(instance) =>
        Future.successful(Right(instance))
    }
  }

  def getInstance(path: NexusPath, id: String, token: String, parameters: List[(String, String)] = List()):Future[Either[WSResponse, NexusInstance]] = {
    wSClient.url(s"$nexusEndpoint/v0/data/${path.toString()}/$id?fields=all&deprecated=false")
      .withQueryStringParameters( parameters: _*)
      .addHttpHeaders("Authorization" -> token).get().map {
      res =>
        res.status match {
          case OK =>
            Right(NexusInstance(res.json))
          case _ =>
            Left(res)
        }
    }
  }

  def retrieveReconciledFromOriginal(originalPath: NexusPath, reconciledOrg:String, id: String, token: String, parameters: List[(String, String)] = List()): Future[Either[WSResponse, Option[NexusInstance]]] = {
    val editorOrg = NexusPath.addSuffixToOrg(originalPath.org, editorPrefix)
    val filter =
      s"""{
         |    "op": "or",
         |    "value": [
         |      {
         |        "op":"eq",
         |        "path":"http://hbp.eu/reconciled#original_parent",
         |        "value": "$nexusEndpoint/v0/data/${originalPath.toString()}/$id"
         |      },
         |      {
         |        "op":"eq",
         |        "path":"http://hbp.eu/reconciled#original_parent",
         |        "value": "$nexusEndpoint/v0/data/${editorOrg}/${originalPath.domain}/${originalPath.schema}/${originalPath.version}/$id"
         |      }
         |    ]

         | }
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    wSClient
      .url(s"$nexusEndpoint/v0/data/${reconciledOrg}/${originalPath.domain}/${originalPath.schema}/${originalPath.version}/?deprecated=false&fields=all&size=1&filter=${URLEncoder.encode(filter, "utf-8")}")
      .withQueryStringParameters( parameters: _*)
      .addHttpHeaders("Authorization" -> token)
      .get()
      .map {
        res =>
          res.status match {
            case OK =>
              if( (res.json \"total").as[Int] > 0){
                Right(
                  Some( NexusInstance(InstanceHelper.removeNexusFields( ((res.json \ "results").as[List[JsValue]].head \ "source").as[JsObject])))
                )
              }else{
                Right(None)
              }
            case _ =>
              Left(res)
          }
      }
  }

  def retrieveInstances(ids: List[String], path: NexusPath, token: String): Future[List[Either[WSResponse, NexusInstance]]] = {
    val listOfRes = for {id <- ids} yield {
      getInstance(path, id, token)
    }
    Future.sequence(listOfRes)
  }


  def upsertUpdateInManualSpace(
                                 destinationOrg: String,
                                 manualEntitiesDetailsOpt: Option[List[UpdateInfo]],
                                 userInfo: User,
                                 instancePath: NexusPath,
                                 manualEntity: JsObject,
                                 token: String
                               ): Future[WSResponse] = {
    manualEntitiesDetailsOpt.flatMap { manualEntitiesDetails =>
      // find manual entry corresponding to the user
      manualEntitiesDetails.filter(_._3 == userInfo.id).headOption.map {
        case (manualEntityId, manualEntityRevision, _) =>
          wSClient.url(s"$nexusEndpoint/v0/data/${NexusInstance.getIdfromURL(manualEntityId)}/?rev=$manualEntityRevision").addHttpHeaders("Authorization" -> token).put(
            manualEntity
          )
      }
    }.getOrElse {
      wSClient.url(s"$nexusEndpoint/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}").addHttpHeaders("Authorization" -> token).post(
        manualEntity + ("http://hbp.eu/manual#updater_id", JsString(userInfo.id))
      )
    }
  }


  def createManualSchemaIfNeeded(
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
    } else {
      Future.successful(true)
    }
  }

  def updateReconciledInstance(
                                manualSpace: String,
                                currentReconciledInstance: NexusInstance,
                                editorInstances: List[NexusInstance],
                                originalInstance: NexusInstance,
                                manualEntity: JsObject,
                                manualEntityId: String,
                                updatedValue: JsObject,
                                token: String,
                                userInfo: User
                              ): Future[WSResponse] = {

    val parentId = (originalInstance.content \ "@id").as[String]
    val revision = (currentReconciledInstance.content \ "nxv:rev").as[Int]
    val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
    val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, NexusInstance(updatedValue), editorInstances, manualEntity, originalInstance.nexusPath, manualEntityId, userInfo, parentRevision, parentId, token)
    updateReconcileInstance( payload, currentReconciledInstance.nexusPath, currentReconciledInstance.nexusUUID, revision, token)

  }

  def insertReconciledInstance(
                                destinationOrg: String,
                                manualSpace: String,
                                originalInstance: NexusInstance,
                                manualEntity: JsObject,
                                manualEntityId: String,
                                updatedValue: JsObject,
                                token: String,
                                userInfo: User
                              ): Future[WSResponse] = {
    val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
    val parentId = (originalInstance.content \ "@id").as[String]
    val payload = ReconciledInstanceHelper.generateReconciledInstance(manualSpace, NexusInstance(updatedValue), List(), manualEntity, originalInstance.nexusPath, manualEntityId, userInfo, parentRevision, parentId, token)
    insertInstance(
      destinationOrg,
      payload,
      originalInstance.nexusPath,
      token
    )
  }
}
