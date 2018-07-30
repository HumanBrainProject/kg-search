
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
package helpers

import common.models.NexusPath
import editor.helper.InstanceHelper.{cleanUpInstanceForSave, generateAlternatives, getPriority, toReconcileFormat}
import editor.models.{IncomingLinksInstances, Instance}
import authentication.models.UserInfo
import org.joda.time.DateTime
import play.api.libs.json._

object ReconciledInstanceHelper {


  // NOTE
  /*
      call to reconcile service api may not be used anymore since all transorfmation happen on fully qualify instances
   */

  def formatForReconcile(manualUpdates: IndexedSeq[JsValue], originJson: JsValue): (JsValue, JsValue) = {
    val allResults = manualUpdates.map((jsonResult) => (jsonResult \ "source").as[JsValue]).+:(originJson)
    val contents = allResults.foldLeft(Json.arr())(
      (array, jsonResult) =>
        array.+:(toReconcileFormat(jsonResult, (jsonResult \ "@id").as[String]))
    )
    val priorities: JsValue = allResults.foldLeft(Json.obj())(
      (jsonObj, jsResult) =>
        jsonObj + ((jsResult \ "@id").as[String], Json.toJson(getPriority((jsResult \ "@id").as[String])))
    )
    (contents, priorities)
  }

  def generateReconciledInstance(
                                  manualSpace: String,
                                  reconciledInstance: Instance,
                                  manualUpdates: IncomingLinksInstances,
                                  manualEntityToBestored: JsObject,
                                  userInfo: UserInfo,
                                  parentRevision: Int,
                                  parentId: String,
                                  token: String
                                ): JsObject = {
    val recInstanceWithParents = addManualUpdateLinksToReconcileInstance(
      manualSpace,
      reconciledInstance,
      manualUpdates,
      manualEntityToBestored
    )
    val cleanUpObject = cleanUpInstanceForSave(recInstanceWithParents)
    addReconciledMandatoryFields(cleanUpObject, recInstanceWithParents.nexusPath, userInfo, parentId, parentRevision)

  }

  /**
    * This method adds all the necessary fields to save an instance to the reconciled space
    *
    * @param originalInstance The instance to be saved
    * @param originalPath     The original nexus path of the instance
    * @param userInfo         The user perfoming the save
    * @param parentId         The id of the instance (this could be a instance from a private space (update) or the manual space (create) )
    * @param parentRevision   The revision of the parent instance at the time of the save
    * @return The instance with all the mandatory fields
    */
  def addReconciledMandatoryFields(
                                    originalInstance: JsObject,
                                    originalPath: NexusPath,
                                    userInfo: UserInfo,
                                    parentId: String,
                                    parentRevision: Int = 1
                                  ): JsObject = {
    originalInstance +
      ("@type" -> JsString(s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}")) +
      ("http://hbp.eu/reconciled#updater_id", JsString(userInfo.id)) +
      ("http://hbp.eu/reconciled#original_rev", JsNumber(parentRevision)) +
      ("http://hbp.eu/reconciled#original_parent", Json.obj("@id" -> JsString(parentId))) +
      ("http://hbp.eu/reconciled#origin", JsString(parentId)) +
      ("http://hbp.eu/reconciled#update_timestamp", JsNumber(new DateTime().getMillis))
  }

  def addManualUpdateLinksToReconcileInstance(manualSpace: String,
                                              reconciledInstance: Instance,
                                              incomingLinks: IncomingLinksInstances,
                                              manualEntityToBeStored: JsObject
                                             ): Instance = {
    val manualUpdates: IndexedSeq[Instance] = incomingLinks.manualInstances
    val currentParent = (reconciledInstance.content \ "http://hbp.eu/reconciled#parents").asOpt[List[JsValue]].getOrElse(List[JsValue]())
    val updatedParents: List[JsValue] = manualUpdates.foldLeft(currentParent) { (acc, manual) =>
      val manualId = Json.obj("@id" -> (manual.content \ "@id").as[String])
      manualId :: acc
    }
    val jsArray = Json.toJson(updatedParents)
    val currentUpdater = (manualEntityToBeStored \ "http://hbp.eu/manual#updater_id").as[String]
    val altJson = generateAlternatives(
      manualUpdates.map(cleanUpInstanceForSave)
        .filter(js => (js \ "http://hbp.eu/manual#updater_id").as[String] != currentUpdater)
        .+:(manualEntityToBeStored)
    )
    val res = reconciledInstance.content
      .+("http://hbp.eu/reconciled#parents" -> jsArray)
      .+("http://hbp.eu/reconciled#alternatives", altJson)
    Instance(reconciledInstance.nexusUUID, reconciledInstance.nexusPath, res)
  }
}
