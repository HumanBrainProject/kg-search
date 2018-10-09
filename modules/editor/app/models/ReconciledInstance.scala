
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

package editor.models

import common.models.{NexusInstance, NexusPath, User}
import editor.helpers.InstanceHelper
import org.joda.time.DateTime
import play.api.libs.json._

case class ReconciledInstance( nexusInstance: NexusInstance){


  def updateManualLinks(newManualUpdateId: String): ReconciledInstance = {
    val currentParent = this.getEditorInstanceIds().getOrElse(List[JsObject]())
    val jsArray = Json.toJson((Json.obj(NexusInstance.Fields.nexusId -> newManualUpdateId) +: currentParent).distinct)
    this.copy(
      this.nexusInstance.copy(content = this.nexusInstance.content + (ReconciledInstance.Fields.parents -> jsArray))
    )
  }
  def cleanManualData(): ReconciledInstance = {
    this.copy(this.nexusInstance.cleanManualData())
  }

  def addAlternatives(manualSpace: String,
                      editorInstances: List[EditorInstance],
                      manualEntityToBeStored: EditorInstance
                     ): ReconciledInstance = {
    val altJson = this.generateAlternatives(
      editorInstances
        .filter(instance => instance.updaterId  != manualEntityToBeStored.updaterId)
        .+:(manualEntityToBeStored)
        .map(s => s.copy( s.nexusInstance.copy(content = s.cleanManualData().nexusInstance.content)))
    )
    val res = this.nexusInstance.content + (ReconciledInstance.Fields.alternatives, altJson)
    ReconciledInstance(NexusInstance(this.nexusInstance.nexusUUID, this.nexusInstance.nexusPath, res))
  }

  def generateAlternatives(manualUpdates: List[EditorInstance]): JsValue = {
    // Alternatives are added per user
    // So for each key we have a list of object containing the user id and the value
    val alternatives: Map[String, Seq[(String, JsValue)]] = manualUpdates
      .map { instance =>
        val dataMap: Map[String, JsValue] = instance.nexusInstance.content
          .-(EditorInstance.Fields.parent)
          .-(EditorInstance.Fields.origin).as[Map[String, JsValue]]
        val userId: String = dataMap(EditorInstance.Fields.updaterId).as[String]
        dataMap.map { case (k, v) => k -> ( userId, v) }
      }.foldLeft(Map.empty[String, Seq[(String, JsValue)]]) { case (map, instance) =>
      //Grouping per field in order to have a map with the field and the list of different alternatives on this field
      val tmp: List[(String, Seq[(String, JsValue)])] = instance.map { case (k, v) => (k, Seq(v)) }.toList ++ map.toList
      tmp.groupBy(_._1).map { case (k, v) => k -> v.flatMap(_._2) }
    }

    val perValue = alternatives.map{
      case (k,v) =>
        val tempMap = v.foldLeft(Map.empty[JsValue, Seq[String]]){case (map, tuple) =>
          val temp: Seq[String] = map.getOrElse(tuple._2, Seq.empty[String])
          map.updated(tuple._2, tuple._1 +: temp)
        }
        k ->  tempMap.toList.sortWith( (el1, el2) => el1._2.length > el2._2.length).map( el =>
          Json.obj("value" -> el._1, "updater_id" -> el._2)
        )
    }
    Json.toJson(
      perValue.-("@type")
        .-(EditorInstance.Fields.updateTimeStamp)
        .-(EditorInstance.Fields.updaterId)
    )
  }

  def getOriginalParent(): JsValue = {
    (this.nexusInstance.content \ ReconciledInstance.Fields.originalParent).as[JsValue]
  }

  def getEditorInstanceIds(): Option[List[JsObject]] = {
    (this.nexusInstance.content \  ReconciledInstance.Fields.parents).asOpt[List[JsObject]]
  }

  def removeNexusFields(): ReconciledInstance = {
    this.copy(this.nexusInstance.removeNexusFields())
  }

  def id(): Option[String]= {
    this.nexusInstance.id()
  }

  /**
    * This method adds all the necessary fields to save an instance to the reconciled space
    *
    * @param originalPath     The original nexus path of the instance
    * @param userInfo         The user perfoming the save
    * @param parentId         The id of the instance (this could be a instance from a private space (update) or the manual space (create) )
    * @param parentRevision   The revision of the parent instance at the time of the save
    * @return The instance with all the mandatory fields
    */
  def addReconciledMandatoryFields(
                                    originalPath: NexusPath,
                                    userInfo: User,
                                    parentId: String,
                                    parentRevision: Long = 1L
                                  ): ReconciledInstance = {
    this.copy(
      this.nexusInstance.copy(
        content = this.nexusInstance.content +
          ("@type" -> JsString(s"http://hbp.eu/${originalPath.org}#${originalPath.schema.capitalize}")) +
          (ReconciledInstance.Fields.updaterId, JsString(userInfo.id)) +
          (ReconciledInstance.Fields.originalRev, JsNumber(parentRevision)) +
          (ReconciledInstance.Fields.originalParent, Json.obj("@id" -> JsString(parentId))) +
          (ReconciledInstance.Fields.origin, JsString(parentId)) +
          (ReconciledInstance.Fields.updateTimeStamp, JsNumber(new DateTime().getMillis)) -
          EditorInstance.Fields.origin -
          EditorInstance.Fields.updateTimeStamp -
          EditorInstance.Fields.parent -
          EditorInstance.Fields.updaterId

      )
    )
  }


  def generateReconciledInstance(
                                  manualSpace: String,
                                  editorInstances: List[EditorInstance],
                                  manualEntityToBestored: EditorInstance,
                                  originalEntityPath: NexusPath,
                                  newManualUpdateId: String,
                                  userInfo: User,
                                  parentRevision: Long,
                                  parentId: String
                                ): ReconciledInstance = {
    this.addAlternatives(
        manualSpace,
        editorInstances,
        manualEntityToBestored
      )
      .updateManualLinks(newManualUpdateId)
      .cleanManualData()
      .addReconciledMandatoryFields(originalEntityPath, userInfo, parentId, parentRevision)
  }




}

object ReconciledInstance {

  val contextOrg = "http://hbp.eu/reconciled#"
  object Fields{
    val parents = s"${contextOrg}parents"
    val alternatives = s"${contextOrg}alternatives"
    val originalParent =  s"${contextOrg}original_parent"
    val updaterId = s"${contextOrg}updater_id"
    val origin = s"${contextOrg}origin"
    val updateTimeStamp = s"${contextOrg}update_timestamp"
    val originalRev = s"${contextOrg}original_rev"
  }


}
