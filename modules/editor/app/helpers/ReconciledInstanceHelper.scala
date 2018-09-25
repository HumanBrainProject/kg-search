
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
package editor.helpers

import common.models.{NexusInstance, NexusPath, User}
import editor.models.{EditorInstance, IncomingLinksInstances, ReconciledInstance}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._

object ReconciledInstanceHelper {

  val logger = Logger(this.getClass)


  // NOTE
  /*
      call to reconcile service api may not be used anymore since all transorfmation happen on fully qualify instances
   */

  def formatForReconcile(manualUpdates: IndexedSeq[JsValue], originJson: JsValue): (JsValue, JsValue) = {
    val allResults = manualUpdates.map((jsonResult) => (jsonResult \ "source").as[JsValue]).+:(originJson)
    val contents = allResults.foldLeft(Json.arr())(
      (array, jsonResult) =>
        array.+:(InstanceHelper.toReconcileFormat(jsonResult, (jsonResult \ "@id").as[String]))
    )
    val priorities: JsValue = allResults.foldLeft(Json.obj())(
      (jsonObj, jsResult) =>
        jsonObj + ((jsResult \ "@id").as[String], Json.toJson(InstanceHelper.getPriority((jsResult \ "@id").as[String])))
    )
    (contents, priorities)
  }

  def generateReconciledInstance(
                                  manualSpace: String,
                                  reconciledInstance: ReconciledInstance,
                                  editorInstances: List[EditorInstance],
                                  manualEntityToBestored: EditorInstance,
                                  originalEntitPath: NexusPath,
                                  newManualUpdateId: String,
                                  userInfo: User,
                                  parentRevision: Long,
                                  parentId: String,
                                  token: String
                                ): ReconciledInstance = {
    val recInstanceWithParents = reconciledInstance.addAlternatives(
      manualSpace,
      editorInstances,
      manualEntityToBestored
    )
    val recWithNewManualUpdate = recInstanceWithParents.updateManualLinks(newManualUpdateId)
    val cleanUpObject = recWithNewManualUpdate.cleanManualData()
    cleanUpObject.addReconciledMandatoryFields(originalEntitPath, userInfo, parentId, parentRevision)
  }




}
