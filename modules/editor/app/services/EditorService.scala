
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

import java.net.URLEncoder

import com.google.inject.Inject
import helpers.InstanceHelper.UpdateInfo
import helpers._
import models.NexusPath
import models.instance.{EditorInstance, NexusInstance, ReconciledInstance}
import models.user.User
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class EditorService @Inject()(wSClient: WSClient,
                              nexusService: NexusService,
                              arangoQueryService: ArangoQueryService,
                              config: ConfigurationService,
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
    arangoQueryService.getInstance(path, id).map[Either[WSResponse, NexusInstance]] {
      case Left(res) => // Check in the original space
        Left(res)
      case Right(instance) =>
        Right(instance)
    }
  }



//  /**
//    *  Fetch a reconciled instance from an instance in the original space
//    * @param originalPath The original nexus path
//    * @param reconciledOrg The name of the reconcile organization
//    * @param id The id of the original instance
//    * @param token The auth token
//    * @param parameters A list of parameters to append to the NEXUS API query.
//    * @return The Reconciled instance
//    */
//  def retrieveReconciledFromOriginal(
//                                      originalPath: NexusPath,
//                                      reconciledOrg: String,
//                                      id: String,
//                                      token: String,
//                                      parameters: List[(String, String)] = List()
//                                    ): Future[Either[WSResponse, Option[ReconciledInstance]]] = {
//    val editorOrg = NexusPath.addSuffixToOrg(originalPath.org, config.editorPrefix)
//    val filter =
//      s"""{
//         |    "op": "or",
//         |    "value": [
//         |      {
//         |        "op":"eq",
//         |        "path":"http://hbp.eu/reconciled#original_parent",
//         |        "value": "${config.nexusEndpoint}/v0/data/${originalPath.toString()}/$id"
//         |      },
//         |      {
//         |        "op":"eq",
//         |        "path":"http://hbp.eu/reconciled#original_parent",
//         |        "value": "${config.nexusEndpoint}/v0/data/${editorOrg}/${originalPath.domain}/${originalPath.schema}/${originalPath.version}/$id"
//         |      }
//         |    ]
//
//         | }
//      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
//    wSClient
//      .url(s"${config.nexusEndpoint}/v0/data/${reconciledOrg}/${originalPath.domain}/" +
//        s"${originalPath.schema}/${originalPath.version}/?deprecated=false&fields=all&size=1&filter=${URLEncoder.encode(filter, "utf-8")}")
//      .withQueryStringParameters(parameters: _*)
//      .addHttpHeaders("Authorization" -> token)
//      .get()
//      .map {
//        res =>
//          res.status match {
//            case OK =>
//              if ((res.json \ "total").as[Int] > 0) {
//                Right(
//                  Some(ReconciledInstance(((res.json \ "results").as[List[JsValue]].head \ "source").as[NexusInstance]))
//                )
//              } else {
//                Right(None)
//              }
//            case _ =>
//              Left(res)
//          }
//      }
//  }


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
          manualEntityId match{
            case Some(fullId) =>
              wSClient.url(s"${config.nexusEndpoint}/v0/data/${NexusInstance.getIdfromURL(fullId)}/?rev=$manualEntityRevision")
                .addHttpHeaders("Authorization" -> token).put(
                editorInstance.nexusInstance.content
              )
            case None =>
              wSClient.url(s"${config.nexusEndpoint}/v0/data/$destinationOrg/${instancePath.domain}/${instancePath.schema}/${instancePath.version}")
                .addHttpHeaders("Authorization" -> token).post(
                editorInstance.nexusInstance.content
              )
          }
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

//  def updateReconciledInstance(
//                                manualSpace: String,
//                                editorInstances: List[EditorInstance],
//                                originalInstance: NexusInstance,
//                                originalPath: NexusPath,
//                                manualEntity: EditorInstance,
//                                manualEntityId: String,
//                                updatedValue: ReconciledInstance,
//                                token: String,
//                                userInfo: User
//                              ): Future[WSResponse] = {
//    val parentId = originalInstance.id()
//    val parentRevision = originalInstance.getRevision()
//    val payload =  updatedValue.generateReconciledInstance(
//        manualSpace,
//        editorInstances,
//        manualEntity,
//        originalPath,
//        manualEntityId,
//        userInfo,
//        parentRevision,
//      s"${config.nexusEndpoint}/v0/data/${parentId.get}"
//      )
//    val revision = updatedValue.nexusInstance.getRevision()
//    updateInstance(
//      payload.nexusInstance,
//      updatedValue.nexusInstance.nexusUUID.get,
//      revision,
//      token
//    )
//
//  }

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

  /**
    * Create domains and schemas to prepare the update of an instance.
    * The calls to the different apis are async but still we ensure that
    * the creation of the domain is done before the creation of the schema
    * @param editorSpace
    * @param reconciledSpace
    * @param originalInstancePath
    * @param token
    * @param techToken
    * @return
    */
  def createDomainsAndSchemasSync(
                                   editorSpace: String,
                                   reconciledSpace: String,
                                   originalInstancePath: NexusPath,
                                   token: String,
                                   techToken: String
                                 ): Future[(Boolean, Boolean)] = {

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
                        originalPath: NexusPath,
                        shouldCreateReconciledInstance: Boolean,
                        manualEntitiesDetailsOpt: Option[List[UpdateInfo]],
                        token: String,
                        techToken: String,
                        editorInstances: List[EditorInstance] = List()
                      ): Future[Either[WSResponse, NexusInstance]] = {
    upsertUpdateInManualSpace(editorSpace, manualEntitiesDetailsOpt,  userInfo, originalInstance.nexusPath, editorSpaceEntityToSave, token).map{res =>
      logger.debug(s"Creation of manual update ${res.body}")
      res.status match {
        case status if status < MULTIPLE_CHOICES =>
//          val newManualUpdateId = (res.json \ "@id").as[String]
//          val processReconciledInstance = if(shouldCreateReconciledInstance){
//            insertReconciledInstance(
//              reconciledSpace,
//              editorSpace,
//              originalInstance,
//              editorSpaceEntityToSave,
//              newManualUpdateId,
//              reconciledInstanceToSave,
//              techToken,
//              userInfo
//            )
//          }else{
//            updateReconciledInstance(
//              editorSpace,
//              editorInstances,
//              originalInstance,
//              originalPath,
//              editorSpaceEntityToSave,
//              newManualUpdateId,
//              reconciledInstanceToSave,
//              techToken,
//              userInfo
//            )
//          }
//          processReconciledInstance.map { re =>
//            re.status match {
//              case s if s < MULTIPLE_CHOICES =>
//                logger.debug(s"Creation of a reconciled instance ${re.body}")
//                Right(reconciledInstanceToSave.nexusInstance)
//              case _ =>
//                logger.error(s"Error while updating a reconciled instance ${re.body}")
//                Left(re)
//            }
//          }
          Right(reconciledInstanceToSave.nexusInstance)
        case _ =>
          logger.error(s"Error while updating a editor instances ${res.body}")
          Left(res)
      }
    }
  }

  def preppingEntitiesForSave(
                               updatedInstance: NexusInstance,
                               currentlyDisplayedInstance: NexusInstance,
                               originalParentId: String,
                               originalPath: NexusPath,
                               userInfo: User,
                               token: String
                             ): EditorInstance = {
    val entityType = s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}"
    val diffEntity = InstanceHelper.buildDiffEntity(currentlyDisplayedInstance, updatedInstance)
    val correctedLinks = EditorInstance(
      NexusInstance(
        None, originalPath.reconciledPath(config.editorPrefix), Json.toJson(diffEntity.value.map {
          case (k, v) => if(!k.startsWith(EditorInstance.contextOrg) && k.startsWith(ReconciledInstance.contextOrg)) {
            recursiveCheckOfIds(k, v, config.reconciledPrefix, token)
          }else{
            k -> v
          }
        }).as[JsObject]
      )
    ).prepareManualEntityForStorage(
      userInfo,
      s"${config.nexusEndpoint}/v0/data/${originalParentId}",
      entityType
    )
    correctedLinks.cleanReconciledFields()
  }


  def recursiveCheckOfIds(k: String,
                          v: JsValue,
                          reconciledPrefix: String,
                          token: String
                         ): (String, JsValue) = {
    if (k == "@id") {
      val url = v.as[String]
      val base = url.split("v0/data/").head
      val (id, path) = NexusInstance.extractIdAndPathFromString(url)
      val reconciledPath = path.reconciledPath(reconciledPrefix)
      val res = Await.result(nexusService.getInstance(reconciledPath, id, token), 10.seconds)
      if (res.isRight) {
        k -> JsString(s"${base}v0/data/${reconciledPath.toString()}/${id}")
      } else {
        k -> JsString(s"${base}v0/data/${path.toString()}/${id}")
      }
    } else {
      v match {
        case v if v.asOpt[JsObject].isDefined =>
          val obj = v.as[JsObject].value.map {
            case (childK, childV) =>
              recursiveCheckOfIds(childK, childV, reconciledPrefix, token)
          }
          k -> Json.toJson(obj)
        case v if v.asOpt[JsArray].isDefined =>
          val arr = v.as[JsArray].value.map { item =>
            if (item.asOpt[JsObject].isDefined) {
              val r = item.as[JsObject].value.map {
                case (childK, childV) =>
                  recursiveCheckOfIds(childK, childV, reconciledPrefix, token)
              }
              Json.toJson(r)
            } else {
              item
            }
          }
          k -> Json.toJson(arr)
        case v => k -> v
      }
    }

  }


}
