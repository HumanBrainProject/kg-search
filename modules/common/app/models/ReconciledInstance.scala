package common.models

import common.helpers.InstanceHelper
import common.models.NexusInstance
import play.api.libs.json.{JsObject, Json}

class ReconciledInstance( nexusInstance: NexusInstance){


  def updateManualLinks(reconciledInstance: NexusInstance, newManualUpdateId: String): NexusInstance = {
    val currentParent = (reconciledInstance.content \ ReconciledInstance.Fields.parents).asOpt[List[JsObject]].getOrElse(List[JsObject]())
    val jsArray = Json.toJson( (Json.obj(NexusInstance.Fields.nexusId -> newManualUpdateId) +: currentParent).distinct)
    val res = reconciledInstance.content
      .+( ReconciledInstance.Fields.parents -> jsArray)
    NexusInstance(reconciledInstance.nexusUUID, reconciledInstance.nexusPath, res)

  }

  def addAlternatives(manualSpace: String,
                      editorInstances: List[EditorInstance],
                      manualEntityToBeStored: EditorInstance
                     ): ReconciledInstance = {
    val altJson = InstanceHelper.generateAlternatives(
      editorInstances
        .filter(instance => instance.updaterId  != manualEntityToBeStored.updaterId)
        .+:(manualEntityToBeStored)
        .map(InstanceHelper.cleanUpInstanceForSave)
    )
    val res = this.nexusInstance.content
      .+("http://hbp.eu/reconciled#alternatives", altJson)
    ReconciledInstance(NexusInstance(this.nexusInstance.nexusUUID, this.nexusInstance.nexusPath, res))
  }

}

object ReconciledInstance {

  val contextOrg = "http://hbp.eu/reconciled#"
  object Fields{
    val parents = s"$contextOrg#parents"
  }
}
