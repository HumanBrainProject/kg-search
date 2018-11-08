
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

import constants.SchemaFieldsConstants
import models._
import models.instance.{EditorInstance, NexusInstance, PreviewInstance, ReconciledInstance}
import org.json4s.JsonAST._
import org.json4s.native.{JsonMethods, JsonParser}
import org.json4s.{Diff, JsonAST}
import play.api.Logger
import play.api.libs.json.Reads.of
import play.api.libs.json._

import scala.collection.immutable.SortedSet

object InstanceHelper {
    val logger = Logger(this.getClass)

  type UpdateInfo = (Option[String], Int, String, EditorInstance)

  def generateInstanceWithReconciliationLogic(
                                               nexusEndpoint: String,
                                               reconciledSuffix:String,
                                               originalInstance: NexusInstance,
                                               editorInstances: List[EditorInstance]
                                             ): (ReconciledInstance, Option[List[UpdateInfo]]) = {
    logger.debug(s"Result from incoming links $editorInstances")
    val updatesByPriority = buildManualUpdatesFieldsFrequency(editorInstances)
    val result = ReconciledInstance(
      NexusInstance(
        originalInstance.id(),
        originalInstance.nexusPath.reconciledPath(reconciledSuffix),
        reconcilationLogic(updatesByPriority, originalInstance.content)
      )
    )
    val manualUpdateDetailsOpt = if(editorInstances.isEmpty){
      logger.debug("creating new editor instance")
      None
    }else{
      //Call reconcile API
      logger.debug(s"Reconciled instance $result")
      val manualUpdateDetails = editorInstances.map(manualEntity => manualEntity.extractUpdateInfo())
      Some(manualUpdateDetails)
    }
    (result, manualUpdateDetailsOpt)
  }

  private def handleDeletion(deleted: JValue, newJson: JValue): JValue = {
    implicit class JValueExtended(value: JValue) {
      def has(childString: String): Boolean = {
        if ((value \ childString) != JNothing) {
          true
        } else {
          false
        }
      }
    }
    /* fields deletion. Deletion are allowed on manual space ONLY
     * final diff is then an addition/update from original view
     * this allows partially defined form (some field are then not updatable)
     */
    logger.debug(s"""PARTIAL FORM DEFINITION - missing fields from form: ${deleted.toString}""")
    // Here we get the diff as being the array without the deleted element
    val deletion = deleted.values.asInstanceOf[Map[String, Any]].map {
      case (k, _) =>
        if (!newJson.has(k)) {
          k -> JNull
        } else {
          k -> newJson \ k
        }
    }
    JObject(deletion.toList.map(x => JField(x._1, x._2)))

  }

  def buildDiffEntity(currentInstance: NexusInstance, newValue: NexusInstance): EditorInstance = {
    val consolidatedJson = JsonParser.parse(currentInstance.removeNexusFields().content.toString())
    val newJson = JsonParser.parse(newValue.content.toString())
    val Diff(changed, added, deleted) = consolidatedJson.diff(newJson)
    val diff: JsonAST.JValue = (deleted, changed) match {
      case (JsonAST.JNothing, JsonAST.JNothing) =>
        added
      case (JsonAST.JNothing, _) =>
        changed.merge(added)
      case (_, JsonAST.JNothing) =>
        val deletion = handleDeletion(deleted, newJson)
        deletion.merge(added)
      case _ =>
        val deletion = handleDeletion(deleted, newJson)
        changed.merge(deletion.merge(added))
    }
    val rendered = Json.parse(JsonMethods.compact(JsonMethods.render(diff))).as[JsObject]
    val diffWithCompleteArray = rendered.value.map {
      case (k, v) =>
        val value = (newValue.content \ k).asOpt[JsValue]
        if (v.asOpt[JsArray].isDefined && value.isDefined) {
          k -> value.get
        } else {
          k -> v
        }
    }
    EditorInstance(
      NexusInstance(currentInstance.nexusUUID, currentInstance.nexusPath, Json.toJson(diffWithCompleteArray).as[JsObject])
    )
  }

  def md5HashString(s: String): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def buildManualUpdatesFieldsFrequency(manualUpdates: List[EditorInstance]): Map[String, SortedSet[(JsValue, Int)]] = {
    val cleanMap: List[Map[String, JsValue]] = manualUpdates.map(s => s.cleanManualData().contentToMap())
    buildMapOfSortedManualUpdates(cleanMap)
  }

  // build reconciled view from updates statistics
  def reconcilationLogic(frequencies: Map[String, SortedSet[(JsValue, Int)]], origin: JsObject): JsObject = {
    // simple logic: keep the most frequent
    val transformations = frequencies.filterKeys(key => !key.startsWith(EditorInstance.contextOrg)).map {
      case (pathString, freqs) => (JsFlattener.buildJsPathFromString(pathString), freqs.last._1)
    }.toSeq
    applyChanges(origin, transformations)
  }

  // apply a set of transformation to an instance
  def applyChanges(instance: JsObject, changes: Seq[(JsPath, JsValue)]): JsObject = {
    val obj = changes.foldLeft(instance) {
      case (res, (path, value)) =>
        val updatedJson = updateJson(res, path, value)
        updatedJson
    }
    obj
  }

  // return an updated JsObject or the src object in case of failure
  def updateJson(instance: JsObject, path: JsPath, newValue: JsValue): JsObject = {
    val simpleUpdateTransformer = path.json.update(
      of[JsValue].map { js =>
          newValue
        }
    )
    instance.transform(simpleUpdateTransformer) match {
      case s: JsSuccess[JsObject] => s.get
      case e: JsError => logger.error(s"Could not apply json update - ${JsError.toJson(e).toString()}")
        throw new Exception("Could not apply update")
    }
  }

  def buildMapOfSortedManualUpdates(manualUpdates: List[Map[String, JsValue]]): Map[String, SortedSet[(JsValue, Int)]] = {
    implicit val order = Ordering.fromLessThan[(JsValue, Int)](_._2 < _._2)
    // For each update
    val tempMap = manualUpdates.foldLeft(Map.empty[String, List[JsValue]]) {
      case (merged, m) =>
        val tempRes: Map[String, List[JsValue]] = m.foldLeft(merged) { case (acc, (k, v)) =>
          acc.get(k) match {
            case Some(existing) => acc.updated(k, v :: existing)
            case None => acc.updated(k, List(v))
          }
        }
        tempRes
    }
    val sortedSet = tempMap
      .filter(e => e._1 != "@type" &&
        e._1 != EditorInstance.Fields.parent &&
        e._1 != EditorInstance.Fields.origin &&
        e._1 != EditorInstance.Fields.updaterId)
      .map { el =>
        val e = el._2.groupBy(identity).mapValues(_.size)
        el._1 -> SortedSet(e.toList: _*)
      }
    sortedSet
  }

  def merge[K, V](maps: Seq[Map[K, V]])(f: (K, V, V) => V): Map[K, V] = {
    maps.foldLeft(Map.empty[K, V]) { case (merged, m) =>
      m.foldLeft(merged) { case (acc, (k, v)) =>
        acc.get(k) match {
          case Some(existing) => acc.updated(k, f(k, existing, v))
          case None => acc.updated(k, v)
        }
      }
    }
  }

  def formatInstanceList(jsArray: JsArray, reconciledSuffix:String): List[PreviewInstance] = {

    jsArray.value.map { el =>
      val url = (el \ "@id").as[String]
      val name: String = (el \ SchemaFieldsConstants.NAME)
        .asOpt[String].getOrElse(
        (el \"http://hbp.eu/minds#alias" ).asOpt[String].getOrElse( (el \ "http://hbp.eu/minds#title").asOpt[String].getOrElse(""))
      )
      val description: String = if ((el \ SchemaFieldsConstants.DESCRIPTION).isDefined) {
        (el \ SchemaFieldsConstants.DESCRIPTION).as[String]
      } else {
        ""
      }
      val id = url.split("/v0/data/").last
      PreviewInstance(id,name, Some(description))
    }.toList
  }

  def toReconcileFormat(jsValue: JsValue, privateSpace: String): JsObject = {
    Json.obj("src" -> privateSpace, "content" -> jsValue.as[JsObject].-("@context"))
  }

  //TODO make it dynamic :D
  def getPriority(id: String): Int = {
    if (id contains "editor/") {
      3
    } else {
      1
    }
  }

  def getCurrentInstanceDisplayed(currentReconciledInstances: Seq[NexusInstance], originalInstance: NexusInstance): NexusInstance = {
    if (currentReconciledInstances.nonEmpty) {
      val sorted = currentReconciledInstances.sortWith { (left, right) =>
        (left.content \ ReconciledInstance.Fields.updateTimeStamp).as[Long] >
          (right.content \ ReconciledInstance.Fields.updateTimeStamp).as[Long]
      }
      sorted.head
    } else{
      originalInstance
    }
  }

  def addDefaultFields(instance: NexusInstance,formRegistry:FormRegistry): NexusInstance = {
    val fields = (formRegistry.registry \ instance.nexusPath.org \ instance.nexusPath.domain \ instance.nexusPath.schema \instance.nexusPath.version \ "fields")
      .as[JsObject].value
    val m = fields.map { case (k, v) =>
      val fieldValue =  instance.getField(k)
      if(fieldValue.isEmpty){
        val formObjectType = (v \ "type").as[String]
        formObjectType match {
          case "DropdownSelect" =>
            k -> JsArray()
          case _ =>
            k -> JsNull
        }
      }else{
        k -> fieldValue.get
      }
    }
    val r = Json.toJson(m).as[JsObject].deepMerge(instance.content)
    instance.copy(content = Json.toJson(r).as[JsObject])
  }

}
