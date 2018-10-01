
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
import java.util.concurrent.Executors

import com.google.inject.Inject
import common.models.{NexusInstance, NexusPath, User}
import editor.models.{EditorInstance, InMemoryKnowledge, IncomingLinksInstances, ReconciledInstance}
import editor.controllers.NexusEditorController
import editor.helpers._
import nexus.helpers.NexusHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import common.services.ConfigurationService
import editor.helpers.InstanceHelper.UpdateInfo
import nexus.helpers.NexusHelper.hash

import scala.concurrent.{ExecutionContext, Future, blocking}

class EditorService @Inject()(wSClient: WSClient,
                              nexusService: NexusService,
                              config: ConfigurationService
                               )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)

  def retrieveIncomingLinks(
                             originalId: String,
                             originalOrg: String,
                             token: String
                           ): Future[IndexedSeq[NexusInstance]] = {
    val filter =
      s"""
         |{"op":"or","value": [{
         |   "op":"eq",
         |   "path":"${config.nexusEndpoint}/vocabs/nexus/core/terms/v0.1.0/organization",
         |   "value": "${config.nexusEndpoint}/v0/organizations/${originalOrg}${config.reconciledPrefix}"
         | }, {
         |   "op":"eq",
         |   "path":"${config.nexusEndpoint}/vocabs/nexus/core/terms/v0.1.0/organization",
         |   "value": "${config.nexusEndpoint}/v0/organizations/${originalOrg}${config.editorPrefix}"
         | }
         | ]
         |}
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    nexusService
      .listAllNexusResult(s"${config.nexusEndpoint}/v0/data/${originalId}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token)
      .map {
        incomingLinks =>
          incomingLinks.map(el => (el \ "source").as[NexusInstance]).toIndexedSeq
      }
  }

  def insertInstance(
                      destinationOrg: String,
                      newInstance: NexusInstance,
                      instancePath: NexusPath,
                      token: String
                    ): Future[WSResponse] = {
    wSClient
      .url(s"${config.nexusEndpoint}/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}")
      .addHttpHeaders("Authorization" -> token).post(newInstance.content)
  }

  def updateInstance(
                      instance: NexusInstance,
                      id: String,
                      revision: Long,
                      token: String
                    ): Future[WSResponse] = {
    wSClient
      .url(s"${config.nexusEndpoint}/v0/data/$id?rev=${revision}")
      .withHttpHeaders("Authorization" -> token).put(instance.content)
  }


  /**
    * Return a instance by its nexus ID
    * Starting by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    *
    * @param path
    * @param id    The id of the instance
    * @param token The user access token
    * @return An error response or an the instance
    */
  def retrieveInstance(path: NexusPath, id: String, token: String, parameters: List[(String, String)] = List()): Future[Either[WSResponse, NexusInstance]] = {
    val reconciledPath = path.reconciledPath(config.reconciledPrefix)
    getInstance(reconciledPath, id, token, parameters).flatMap[Either[WSResponse, NexusInstance]] {
      case Left(_) => // Check in the original space
        retrieveReconciledFromOriginal(path, reconciledPath.org, id, token, parameters).flatMap[Either[WSResponse, NexusInstance]] {
          case Left(r) =>
            Future.successful(Left(r))
          case Right(reconciledInstance) =>
            if (reconciledInstance.isDefined) {
              Future.successful(Right(reconciledInstance.get.nexusInstance))
            } else {
              getInstance(path, id, token, parameters)
            }
        }
      case Right(instance) =>
        Future.successful(Right(instance))
    }
  }

  def getInstance(path: NexusPath, id: String, token: String, parameters: List[(String, String)] = List()): Future[Either[WSResponse, NexusInstance]] = {
    wSClient.url(s"${config.nexusEndpoint}/v0/data/${path.toString()}/$id?fields=all&deprecated=false")
      .withQueryStringParameters(parameters: _*)
      .addHttpHeaders("Authorization" -> token).get().map {
      res =>
        res.status match {
          case OK =>
            Right(res.json.as[NexusInstance])
          case _ =>
            Left(res)
        }
    }
  }

  def retrieveReconciledFromOriginal(
                                      originalPath: NexusPath,
                                      reconciledOrg: String,
                                      id: String,
                                      token: String,
                                      parameters: List[(String, String)] = List()
                                    ): Future[Either[WSResponse, Option[ReconciledInstance]]] = {
    val editorOrg = NexusPath.addSuffixToOrg(originalPath.org, config.editorPrefix)
    val filter =
      s"""{
         |    "op": "or",
         |    "value": [
         |      {
         |        "op":"eq",
         |        "path":"http://hbp.eu/reconciled#original_parent",
         |        "value": "${config.nexusEndpoint}/v0/data/${originalPath.toString()}/$id"
         |      },
         |      {
         |        "op":"eq",
         |        "path":"http://hbp.eu/reconciled#original_parent",
         |        "value": "${config.nexusEndpoint}/v0/data/${editorOrg}/${originalPath.domain}/${originalPath.schema}/${originalPath.version}/$id"
         |      }
         |    ]

         | }
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    wSClient
      .url(s"${config.nexusEndpoint}/v0/data/${reconciledOrg}/${originalPath.domain}/" +
        s"${originalPath.schema}/${originalPath.version}/?deprecated=false&fields=all&size=1&filter=${URLEncoder.encode(filter, "utf-8")}")
      .withQueryStringParameters(parameters: _*)
      .addHttpHeaders("Authorization" -> token)
      .get()
      .map {
        res =>
          res.status match {
            case OK =>
              if ((res.json \ "total").as[Int] > 0) {
                Right(
                  Some(ReconciledInstance(((res.json \ "results").as[List[JsValue]].head \ "source").as[NexusInstance]))
                )
              } else {
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
                                 manualEntity: EditorInstance,
                                 token: String
                               ): Future[WSResponse] = {
    manualEntitiesDetailsOpt.flatMap { manualEntitiesDetails =>
      // find manual entry corresponding to the user
      manualEntitiesDetails.find(_._3 == userInfo.id).map {
        case (manualEntityId, manualEntityRevision, _, editorInstance) =>
          val userEditorUpdate = editorInstance.cleanManualData().nexusInstance.content.deepMerge(manualEntity.nexusInstance.content)
          wSClient.url(s"${config.nexusEndpoint}/v0/data/${NexusInstance.getIdfromURL(manualEntityId)}/?rev=$manualEntityRevision")
            .addHttpHeaders("Authorization" -> token).put(
            userEditorUpdate
          )
      }
    }.getOrElse {
      wSClient.url(s"${config.nexusEndpoint}/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}")
        .addHttpHeaders("Authorization" -> token).post(
        manualEntity.nexusInstance.content
      )
    }
  }


  def createManualSchemaIfNeeded(
                                  originalInstanceNexusPath: NexusPath,
                                  token: String,
                                  destinationOrg: String,
                                  editorOrg: String,
                                  editorContext: String = ""
                                ): Future[Boolean] = {
    // ensure schema related to manual update exists or create it
    nexusService.createSchema(
      config.nexusEndpoint,
      destinationOrg,
      originalInstanceNexusPath,
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
  }

  def updateReconciledInstance(
                                manualSpace: String,
                                currentReconciledInstance: ReconciledInstance,
                                editorInstances: List[EditorInstance],
                                originalInstance: NexusInstance,
                                originalPath: NexusPath,
                                manualEntity: EditorInstance,
                                manualEntityId: String,
                                updatedValue: ReconciledInstance,
                                token: String,
                                userInfo: User
                              ): Future[WSResponse] = {
    val parentId = originalInstance.id()
    val revision = currentReconciledInstance.nexusInstance.getRevision()
    val parentRevision = originalInstance.getRevision()
    val payload =  updatedValue.generateReconciledInstance(
        manualSpace,
        editorInstances,
        manualEntity,
        originalPath,
        manualEntityId,
        userInfo,
        parentRevision,
        parentId.get
      )
    updateInstance(
      payload.nexusInstance,
      currentReconciledInstance.nexusInstance.nexusUUID.get,
      revision,
      token
    )

  }

  def insertReconciledInstance(
                                destinationOrg: String,
                                manualSpace: String,
                                originalInstance: NexusInstance,
                                manualEntity: EditorInstance,
                                manualEntityId: String,
                                updatedValue: ReconciledInstance,
                                token: String,
                                userInfo: User
                              ): Future[WSResponse] = {
    val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
    val parentId = (originalInstance.content \ "@id").as[String]
    val payload = updatedValue.generateReconciledInstance(
      manualSpace,
      List(),
      manualEntity,
      originalInstance.nexusPath,
      manualEntityId,
      userInfo,
      parentRevision,
      parentId
    )
    insertInstance(
      destinationOrg,
      payload.nexusInstance,
      originalInstance.nexusPath,
      token
    )
  }

  def createDomainsAndSchemasSync(editorSpace: String, reconciledSpace: String, originalInstancePath: NexusPath, token: String, techToken: String): Future[(Boolean, Boolean)] = {

    val createEditorDomain = nexusService
      .createDomain(
        config.nexusEndpoint,
        editorSpace,
        originalInstancePath.domain,
        "",
        token
      ).flatMap(res =>
      createManualSchemaIfNeeded(
        originalInstancePath,
        token,
        editorSpace,
        "manual",
        EditorSpaceHelper.nexusEditorContext("manual")
      )
    )

    val createReconciledDomain = nexusService
      .createDomain(
        config.nexusEndpoint,
        reconciledSpace,
        originalInstancePath.domain,
        "",
        techToken
      ).flatMap(res =>
      createManualSchemaIfNeeded(
        originalInstancePath,
        techToken,
        reconciledSpace,
        "reconciled",
        EditorSpaceHelper.nexusEditorContext("reconciled")
      )
    )
    createEditorDomain.flatMap(b1 => createReconciledDomain.map(b2 => (b1,b2)))

  }

  def saveEditorUpdate(
                        editorSpace: String,
                        reconciledSpace: String,
                        userInfo: User,
                        editorSpaceEntityToSave: EditorInstance,
                        reconciledInstanceToSave: ReconciledInstance,
                        originalInstance: NexusInstance,
                        shouldCreateReconciledInstance: Boolean,
                        manualEntitiesDetailsOpt: Option[List[UpdateInfo]],
                        token: String,
                        techToken: String,
                        editorInstances: List[EditorInstance] = List()
                      ): Future[Either[WSResponse, NexusInstance]] = {
    upsertUpdateInManualSpace(editorSpace, manualEntitiesDetailsOpt,  userInfo, originalInstance.nexusPath, editorSpaceEntityToSave, token).flatMap{res =>
      logger.debug(s"Creation of manual update ${res.body}")
      res.status match {
        case status if status < MULTIPLE_CHOICES =>
          val newManualUpdateId = (res.json \ "@id").as[String]
          val processReconciledInstance = if(shouldCreateReconciledInstance){
            insertReconciledInstance(
              reconciledSpace,
              editorSpace,
              originalInstance,
              editorSpaceEntityToSave,
              newManualUpdateId,
              reconciledInstanceToSave,
              techToken,
              userInfo
            )
          }else{
            updateReconciledInstance(
              editorSpace,
              reconciledInstanceToSave,
              editorInstances,
              originalInstance,
              originalInstance.nexusPath,
              editorSpaceEntityToSave,
              newManualUpdateId,
              reconciledInstanceToSave,
              token,
              userInfo
            )
          }
          processReconciledInstance.map { re =>
            re.status match {
              case s if s < MULTIPLE_CHOICES =>
                logger.debug(s"Creation of a reconciled instance ${re.body}")
                Right(reconciledInstanceToSave.nexusInstance)
              case _ =>
                logger.error(s"Error while updating a reconciled instance ${re.body}")
                Left(re)
            }
          }
        case _ => Future{
          logger.error(s"Error while updating a editor instances ${res.body}")
          Left(res)
        }
      }
    }
  }

}
