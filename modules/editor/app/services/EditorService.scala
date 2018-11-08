
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
import helpers._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.user.User
import models.{FormRegistry, NexusPath}
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}
import services.instance.InstanceApiService

import scala.concurrent.{ExecutionContext, Future}

class EditorService @Inject()(
                               wSClient: WSClient,
                               config: ConfigurationService,
                             )(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService

  //  def retrieveIncomingLinks(
  //                             originalId: String,
  //                             originalOrg: String,
  //                             token: String
  //                           ): Future[IndexedSeq[NexusInstance]] = {
  //    val filter =
  //      s"""
  //         |{"op":"or","value": [{
  //         |   "op":"eq",
  //         |   "path":"${config.nexusEndpoint}/vocabs/nexus/core/terms/v0.1.0/organization",
  //         |   "value": "${config.nexusEndpoint}/v0/organizations/${originalOrg}${config.reconciledPrefix}"
  //         | }, {
  //         |   "op":"eq",
  //         |   "path":"${config.nexusEndpoint}/vocabs/nexus/core/terms/v0.1.0/organization",
  //         |   "value": "${config.nexusEndpoint}/v0/organizations/${originalOrg}${config.editorPrefix}"
  //         | }
  //         | ]
  //         |}
  //      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
  //    nexusService
  //      .listAllNexusResult(s"${config.nexusEndpoint}/v0/data/${originalId}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token)
  //      .map {
  //        incomingLinks =>
  //          incomingLinks.map(el => (el \ "source").as[NexusInstance]).toIndexedSeq
  //      }
  //  }

  def insertInstance(
                      newInstance: NexusInstance,
                      token: String
                    ): Future[Either[WSResponse, NexusInstance]] = {
    instanceApiService.post(wSClient, config.kgQueryEndpoint, newInstance, newInstance.nexusPath, token)
  }

  /**
    * Updating an instance
    *
    * @param diffInstance           The diff of the current instance and its modification
    * @param nexusInstanceReference The reference of the instance to update
    * @param token                  the user token
    * @param userId                 the id of the user sending the update
    * @return The updated instance
    */
  def updateInstance(
                      diffInstance: EditorInstance,
                      nexusInstanceReference: NexusInstanceReference,
                      token: String,
                      userId: String
                    ): Future[Either[WSResponse, Unit]] = {
    instanceApiService.put(wSClient, config.kgQueryEndpoint, nexusInstanceReference, diffInstance, token, userId)
  }


  /**
    * Return a instance by its nexus ID
    * Starting by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    *
    * @param nexusInstanceReference The reference to the instace to retrieve
    * @param token                  The user access token
    * @return An error response or an the instance
    */
  def retrieveInstance(nexusInstanceReference: NexusInstanceReference, token: String): Future[Either[WSResponse, NexusInstance]] = {
    instanceApiService.get(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
  }


  def generateDiffAndUpdateInstance(
                                     instanceRef:NexusInstanceReference,
                                     updateFromUser: JsValue,
                                     token:String,
                                     user: User,
                                     formRegistry: FormRegistry
                                   ): Future[Either[WSResponse, Unit]] = {
    retrieveInstance(instanceRef, token).flatMap {
      case Left(res) =>
        Future(Left(res))
      case Right(currentInstanceDisplayed) =>
        val instanceUpdateFromUser = FormService.buildInstanceFromForm(currentInstanceDisplayed, updateFromUser, config.nexusEndpoint)
        val updateToBeStored = InstanceHelper.buildDiffEntity(currentInstanceDisplayed.removeNexusFields(), instanceUpdateFromUser)
        updateInstance(updateToBeStored, instanceRef, token, user.id)
    }
  }

  //  def saveEditorUpdate(
  //                        editorSpace: String,
  //                        reconciledSpace: String,
  //                        userInfo: User,
  //                        editorSpaceEntityToSave: EditorInstance,
  //                        reconciledInstanceToSave: ReconciledInstance,
  //                        originalInstance: NexusInstance,
  //                        originalPath: NexusPath,
  //                        shouldCreateReconciledInstance: Boolean,
  //                        manualEntitiesDetailsOpt: Option[List[UpdateInfo]],
  //                        token: String,
  //                        techToken: String,
  //                        editorInstances: List[EditorInstance] = List()
  //                      ): Future[Either[WSResponse, NexusInstance]] = {
  //    upsertUpdateInManualSpace(editorSpace, manualEntitiesDetailsOpt,  userInfo, originalInstance.nexusPath, editorSpaceEntityToSave, token).map{res =>
  //      logger.debug(s"Creation of manual update ${res.body}")
  //      res.status match {
  //        case status if status < MULTIPLE_CHOICES =>
  ////          val newManualUpdateId = (res.json \ "@id").as[String]
  ////          val processReconciledInstance = if(shouldCreateReconciledInstance){
  ////            insertReconciledInstance(
  ////              reconciledSpace,
  ////              editorSpace,
  ////              originalInstance,
  ////              editorSpaceEntityToSave,
  ////              newManualUpdateId,
  ////              reconciledInstanceToSave,
  ////              techToken,
  ////              userInfo
  ////            )
  ////          }else{
  ////            updateReconciledInstance(
  ////              editorSpace,
  ////              editorInstances,
  ////              originalInstance,
  ////              originalPath,
  ////              editorSpaceEntityToSave,
  ////              newManualUpdateId,
  ////              reconciledInstanceToSave,
  ////              techToken,
  ////              userInfo
  ////            )
  ////          }
  ////          processReconciledInstance.map { re =>
  ////            re.status match {
  ////              case s if s < MULTIPLE_CHOICES =>
  ////                logger.debug(s"Creation of a reconciled instance ${re.body}")
  ////                Right(reconciledInstanceToSave.nexusInstance)
  ////              case _ =>
  ////                logger.error(s"Error while updating a reconciled instance ${re.body}")
  ////                Left(re)
  ////            }
  ////          }
  //          Right(reconciledInstanceToSave.nexusInstance)
  //        case _ =>
  //          logger.error(s"Error while updating a editor instances ${res.body}")
  //          Left(res)
  //      }
  //    }
  //  }

}
