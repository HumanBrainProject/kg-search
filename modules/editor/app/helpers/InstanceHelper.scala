
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

package editor.helper


import common.helpers.JsFlattener
import common.models.NexusPath
import editor.helpers.FormHelper
import editor.models.{InMemoryKnowledge, Instance}
import models.authentication.UserInfo
import nexus.helpers.NexusHelper
import org.joda.time.DateTime
import org.json4s.native.{JsonMethods, JsonParser}
import org.json4s.{Diff, JsonAST}
import play.api.Logger
import play.api.libs.json.Reads.of
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{AnyContent, Request}
import play.api.http.Status._

import scala.collection.immutable.SortedSet
import scala.concurrent.{ExecutionContext, Future}

object InstanceHelper {
    val logger = Logger(this.getClass)

  def retrieveIncomingLinks(nexusEndpoint:String, originalInstance: Instance,
                            token: String)(implicit ws: WSClient, ec: ExecutionContext): Future[IndexedSeq[Instance]] = {
    val filter =
      """
        |{"op":"or","value": [{
        |   "op":"eq",
        |   "path":"https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "https://nexus-dev.humanbrainproject.org/v0/organizations/reconciled"
        | }, {
        |   "op":"eq",
        |   "path":"https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/organization",
        |   "value": "https://nexus-dev.humanbrainproject.org/v0/organizations/manual"
        | }
        | ]
        |}
      """.stripMargin.stripLineEnd.replaceAll("\r\n", "")
    NexusHelper.listAllNexusResult(s"$nexusEndpoint/v0/data/${originalInstance.id()}/incoming?deprecated=false&fields=all&size=50&filter=$filter", token).map {
      incomingLinks =>
        incomingLinks.map(el => Instance((el \ "source").as[JsValue])).toIndexedSeq
    }
  }


  def generateReconciledInstance(manualSpace: String, reconciledInstance: Instance, manualUpdates: IndexedSeq[Instance], manualEntityToBestored: JsObject, userInfo: UserInfo, parentRevision: Int, parentId: String, token: String): JsObject = {

    val recInstanceWithParents = addManualUpdateLinksToReconcileInstance(manualSpace, reconciledInstance, manualUpdates, manualEntityToBestored)
    val cleanUpObject = cleanUpInstanceForSave(recInstanceWithParents).+("@type" -> JsString(s"http://hbp.eu/reconciled#${reconciledInstance.nexusPath.schema.capitalize}"))
    cleanUpObject +
      ("http://hbp.eu/reconciled#updater_id", JsString(userInfo.id)) +
      ("http://hbp.eu/reconciled#original_rev", JsNumber(parentRevision)) +
      ("http://hbp.eu/reconciled#original_parent", Json.obj("@id" -> JsString(parentId))) +
      ("http://hbp.eu/reconciled#origin", JsString(parentId)) +
      ("http://hbp.eu/reconciled#update_timestamp", JsNumber(new DateTime().getMillis))
  }

  def createReconcileInstance(nexusEndpoint:String,reconciledSpace: String, instance: JsObject, schema: String, version: String, token: String)(implicit ws: WSClient): Future[WSResponse] = {
    ws.url(s"$nexusEndpoint/v0/data/$reconciledSpace/${schema}/${version}").withHttpHeaders("Authorization" -> token).post(instance)
  }

  def updateReconcileInstance(nexusEndpoint:String,reconciledSpace:String, instance: JsObject, nexusPath: NexusPath, id: String, revision: Int, token: String)(implicit ws: WSClient): Future[WSResponse] = {
    ws.url(s"$nexusEndpoint/v0/data/$reconciledSpace/${nexusPath.schema}/${nexusPath.version}/$id?rev=${revision}").withHttpHeaders("Authorization" -> token).put(instance)
  }

  def addManualUpdateLinksToReconcileInstance(manualSpace:String, reconciledInstance: Instance, incomingLinks: IndexedSeq[Instance], manualEntityToBeStored: JsObject): Instance = {
    val manualUpdates: IndexedSeq[Instance] = incomingLinks.filter(instance => instance.nexusPath.toString() contains manualSpace)
    val currentParent = (reconciledInstance.content \ "http://hbp.eu/reconciled#parents").asOpt[List[JsValue]].getOrElse(List[JsValue]())
    val updatedParents: List[JsValue] = manualUpdates.foldLeft(currentParent) { (acc, manual) =>
      val manualId = Json.obj("@id" -> (manual.content \ "@id").as[String])
      manualId :: acc
    }
    val jsArray = Json.toJson(updatedParents)
    val currentUpdater = (manualEntityToBeStored \ "http://hbp.eu/manual#updater_id").as[String]
    val altJson = generateAlternatives(
      manualUpdates.map(cleanUpInstanceForSave)
        .filter(js => (js \ "http://hbp.eu/manual#updater_id").as[String] != currentUpdater).+:(manualEntityToBeStored)
    )
    val res = reconciledInstance.content.+("http://hbp.eu/reconciled#parents" -> jsArray).+("http://hbp.eu/reconciled#alternatives", altJson)
    Instance(reconciledInstance.nexusUUID, reconciledInstance.nexusPath, res)
  }




  type UpdateInfo = (String, Int, String)

  def consolidateFromManualSpace(
                                  manualSpace:String,
                                  originalInstance: Instance,
                                  incomingLinks: IndexedSeq[Instance],
                                  updateToBeStoredInManual: JsObject
                                ): (Instance, Option[IndexedSeq[UpdateInfo]]) = {
    val manualUpdates = incomingLinks.filter(instance => instance.nexusPath.toString() contains manualSpace)
    logger.debug(s"Result from incoming links $manualUpdates")
    if (manualUpdates.nonEmpty) {
      val manualUpdateDetails = manualUpdates.map(manualEntity => manualEntity.extractUpdateInfo())
      //Call reconcile API
      val originJson = originalInstance.content
      val updatesByPriority = buildManualUpdatesFieldsFrequency(manualUpdates, updateToBeStoredInManual)
      //TODO Check the result is correct this works
      val result = Instance(reconcilationLogic(updatesByPriority, originJson))
      logger.debug(s"Reconciled instance $result")
      (result, Some(manualUpdateDetails))
    } else {
      val consolidatedInstance = buildInstanceFromForm(originalInstance.content, updateToBeStoredInManual)
      (Instance(consolidatedInstance), None)
    }
  }

  def retrieveOriginalInstance(nexusEndpoint:String, path: NexusPath, id:String, token: String)(implicit ws: WSClient, ec: ExecutionContext): Future[Either[WSResponse, Instance]] = {
    ws.url(s"$nexusEndpoint/v0/data/${path.toString()}/$id?fields=all").addHttpHeaders("Authorization" -> token).get().map {
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

  def buildDiffEntity(consolidatedResponse: Instance, newValue: String, originalInstance: Instance): JsObject = {
    val consolidatedJson = JsonParser.parse(cleanInstanceManual(consolidatedResponse.content).toString())
    val newJson = JsonParser.parse(newValue)
    val Diff(changed, added, deleted) = consolidatedJson.diff(newJson)
    val diff: JsonAST.JValue = (deleted, changed) match {
      case (JsonAST.JNothing, JsonAST.JNothing) =>
        consolidatedJson
      case (JsonAST.JNothing, _) =>
        changed.merge(added)
      case _ =>
        /* fields deletion. Deletion are allowed on manual space ONLY
         * final diff is then an addition/update from original view
         * this allows partially defined form (some field are then not updatable)
         */
        val Diff(changedFromOrg, addedFromOrg, deletedFromOrg) = JsonParser.parse(originalInstance.content.toString()).diff(newJson)
        if (deletedFromOrg != JsonAST.JNothing) {
          logger.debug(s"""PARTIAL FORM DEFINITION - missing fields from form: ${deletedFromOrg.toString}""")
        }
        changedFromOrg.merge(addedFromOrg)
    }

    Json.parse(
      JsonMethods.compact(JsonMethods.render(diff))).as[JsObject] +
      ("http://hbp.eu/manual#origin", JsString((originalInstance.content \ "links" \ "self").as[String].split("/").last)) +
      ("http://hbp.eu/manual#parent", Json.obj("@id" -> (originalInstance.content \ "links" \ "self").get.as[JsString]))
  }

  def buildInstanceFromForm(original: JsObject, formContent: JsObject, nexusEndpoint: String): JsObject = {
//    val flattened = JsFlattener(formContent)
//    applyChanges(original, flattened)
    val cleanForm = FormHelper.removeKey(formContent.as[JsValue])
    val formWithID = cleanForm.toString().replaceAll("""id":"""", s"""@id":"${nexusEndpoint}/v0/data/""")
    val res= original.deepMerge(Json.parse(formWithID).as[JsObject])
    res
  }

  def buildNewInstanceFromForm(formContent: JsObject): JsObject = {
    val m = formContent.value.map{ case (k, v) =>
      (v \ "type").as[String] match {
        case "DropdownSelect" =>
          FormHelper.unescapeSlash(k) -> Json.obj("@id" -> (v \ "value").as[JsString])
        case _ =>
          FormHelper.unescapeSlash(k) -> (v \ "value").as[JsString]
      }
    }
    Json.toJson(m).as[JsObject]
  }

  def md5HashString(s: String): String = {
    import java.security.MessageDigest
    import java.math.BigInteger
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1,digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def upsertReconciledInstance(
                                nexusEndpoint: String,
                                reconciledSpace:String,
                                manualSpace: String,
                                instances: IndexedSeq[Instance], originalInstance: Instance,
                                manualEntity: JsObject, updatedValue: JsObject,
                                consolidatedInstance: Instance, token: String,
                                userInfo: UserInfo,
                                inMemoryReconciledSpaceSchemas: InMemoryKnowledge
                              )(implicit ws: WSClient, ec: ExecutionContext): Future[WSResponse] = {

    val reconcileInstances = instances.filter(instance => instance.nexusPath.toString() contains reconciledSpace)
    val parentId = (originalInstance.content \ "@id").as[String]
    if (reconcileInstances.nonEmpty) {
      val reconcileInstance = reconcileInstances.head
      val parentRevision = (reconcileInstance.content \ "nxv:rev").as[Int]
      val payload = generateReconciledInstance(manualSpace, Instance(updatedValue), instances, manualEntity, userInfo, parentRevision, parentId, token)
      updateReconcileInstance(nexusEndpoint, reconciledSpace, payload, reconcileInstance.nexusPath, reconcileInstance.nexusUUID, parentRevision, token)
    } else {
      val parentRevision = (originalInstance.content \ "nxv:rev").as[Int]
      val payload = generateReconciledInstance(manualSpace, consolidatedInstance, instances, manualEntity, userInfo, parentRevision, parentId, token)
      createManualSchemaIfNeeded(nexusEndpoint, updatedValue, originalInstance.nexusPath, token, inMemoryReconciledSpaceSchemas, reconciledSpace, "reconciled").flatMap {
        res =>
          createReconcileInstance(nexusEndpoint, reconciledSpace, payload, consolidatedInstance.nexusPath.schema, consolidatedInstance.nexusPath.version, token)
      }
    }
  }

  def createManualSchemaIfNeeded(
                                  nexusEndpoint:String,
                                  manualEntity: JsObject,
                                  originalInstanceNexusPath: NexusPath,
                                  token: String,
                                  schemasHashMap: InMemoryKnowledge,
                                 space: String,
                                  destinationOrg: String
                                )
                                (implicit ws: WSClient, ec: ExecutionContext): Future[Boolean] = {
    // ensure schema related to manual update exists or create it
    if (manualEntity != JsNull) {
      if (schemasHashMap.manualSchema.isEmpty) { // initial load
        schemasHashMap.loadManualSchemaList(token)
      }
      if (!schemasHashMap.manualSchema.contains(s"$nexusEndpoint/v0/schemas/$space/${originalInstanceNexusPath.schema}/${originalInstanceNexusPath.version}")) {
        NexusHelper.createSchema(nexusEndpoint, destinationOrg, originalInstanceNexusPath.schema.capitalize, space, originalInstanceNexusPath.version, token).map {
          response =>
            response.status match {
              case OK => schemasHashMap.loadManualSchemaList(token)
                logger.info(s"Schema created properly for : " +
                  s"$space/${originalInstanceNexusPath.schema}/${originalInstanceNexusPath.version}")
                Future.successful(true)
              case _ => logger.error(s"ERROR - schema does not exist and " +
                s"automatic creation failed - ${response.body}")
                Future.successful(false)
            }
        }

      }
    }
    Future.successful(true)
  }

  def upsertUpdateInManualSpace(
                                 nexusEndpoint:String,
                                 manualSpace: String,
                                 manualEntitiesDetailsOpt: Option[IndexedSeq[UpdateInfo]],
                                 userInfo: UserInfo,
                                 schema: String,
                                 manualEntity: JsObject,
                                 token: String
                               )
                               (implicit ws: WSClient): Future[WSResponse] = {
    manualEntitiesDetailsOpt.flatMap { manualEntitiesDetails =>
      // find manual entry corresponding to the user
      manualEntitiesDetails.filter(_._3 == userInfo.id).headOption.map {
        case (manualEntityId, manualEntityRevision, _) =>
          ws.url(s"$nexusEndpoint/v0/data/${Instance.getIdfromURL(manualEntityId)}/?rev=$manualEntityRevision").addHttpHeaders("Authorization" -> token).put(
            manualEntity
          )
      }
    }.getOrElse {
      ws.url(s"$nexusEndpoint/v0/data/$manualSpace/${schema}/v0.0.4").addHttpHeaders("Authorization" -> token).post(
        manualEntity + ("http://hbp.eu/manual#updater_id", JsString(userInfo.id))
      )
    }
  }

  def buildManualUpdatesFieldsFrequency(manualUpdates: IndexedSeq[Instance], currentUpdate: JsObject): Map[String, SortedSet[(JsValue, Int)]] = {
    val cleanMap: IndexedSeq[Map[String, JsValue]] = currentUpdate.as[Map[String, JsValue]] +: cleanListManualData(manualUpdates)
    buildMapOfSortedManualUpdates(cleanMap)
  }

  // build reconciled view from updates statistics
  def reconcilationLogic(frequencies: Map[String, SortedSet[(JsValue, Int)]], origin: JsObject): JsObject = {
    // simple logic: keep the most frequent
    val transformations = frequencies.map {
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

    if(instance.transform(simpleUpdateTransformer).isError){
      logger.error(s"Could not transform $newValue")
    }else{
      logger.debug(s"Ok no worries $newValue")
    }
    instance
  }

  def buildMapOfSortedManualUpdates(manualUpdates: IndexedSeq[Map[String, JsValue]]): Map[String, SortedSet[(JsValue, Int)]] = {
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
        e._1 != "http://hbp.eu/manual#parent" &&
        e._1 != "http://hbp.eu/manual#origin" &&
        e._1 != "http://hbp.eu/manual#updater_id")
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

  def cleanListManualData(manualUpdates: IndexedSeq[Instance]): IndexedSeq[Map[String, JsValue]] = {
    manualUpdates.map(el => cleanManualDataFromNexus(el.content))
  }

  def cleanManualDataFromNexus(jsObject: JsObject): Map[String, JsValue] = {
    cleanManualData(jsObject).fields.toMap
  }

  def cleanManualData(jsObject: JsObject): JsObject = {
    jsObject.-("@id").-("@type").-("links").-("nxv:rev").-("nxv:deprecated")
  }

  def cleanUpInstanceForSave(instance: Instance): JsObject = {
    val jsonObj = instance.content
    jsonObj - ("@context") - ("@type") - ("@id") - ("nxv:rev") - ("nxv:deprecated") - ("links")
  }

  def prepareManualEntityForStorage(manualEntity: JsObject, userInfo: UserInfo): JsObject = {
    manualEntity.+("http://hbp.eu/manual#updater_id", JsString(userInfo.id))
      .+("http://hbp.eu/manual#update_timestamp", JsNumber(new DateTime().getMillis))
      .-("@context")
      .-("@id")
      .-("links")
      .-("nxv:rev")
      .-("nxv:deprecated")
  }


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


  def cleanInstanceManual(jsObject: JsObject): JsObject = {
    jsObject.-("http://hbp.eu/manual#parent")
      .-("http://hbp.eu/manual#origin")
      .-("http://hbp.eu/manual#updater_id")
  }

  def formatInstanceList(jsArray: JsArray): JsValue = {

    val arr = jsArray.value.map { el =>
      val id = (el \ "instance" \ "value").as[String]
      val name = (el \ "name" \ "value").as[JsString]
      val description: JsString = if ((el \ "description").isDefined) {
        (el \ "description" \ "value").as[JsString]
      } else {
        JsString("")
      }
      Json.obj("id" -> Instance.getIdfromURL(id), "description" -> description, "label" -> name)
    }
    Json.toJson(arr)
  }

  def formatFromNexusToOption(jsObject: JsObject): JsObject = {
    val id = (jsObject \ "@id").as[String]
    val name = (jsObject \ "http://schema.org/name").as[JsString]
    val description: JsString = if ((jsObject \ "http://schema.org/description").isDefined) {
      (jsObject \ "http://schema.org/description").as[JsString]
    } else { JsString("") }
    Json.obj("id" -> Instance.getIdfromURL(id), "description" -> description, "label" -> name)
  }

  def toReconcileFormat(jsValue: JsValue, privateSpace: String): JsObject = {
    Json.obj("src" -> privateSpace, "content" -> jsValue.as[JsObject].-("@context"))
  }

  //TODO make it dynamic :D
  def getPriority(id: String): Int = {
    if (id contains "manual/poc") {
      3
    } else {
      1
    }
  }

  def getCurrentInstanceDisplayed(currentReconciledInstances: Seq[Instance], originalInstance: Instance): Instance = {
    if (currentReconciledInstances.nonEmpty) {
      val sorted = currentReconciledInstances.sortWith { (left, right) =>
        (left.content \ "http://hbp.eu/reconciled#update_timestamp").as[Long] >
          (right.content \ "http://hbp.eu/reconciled#update_timestamp").as[Long]
      }
      sorted.head
    } else{
      originalInstance
    }
  }

  def generateAlternatives(manualUpdates: IndexedSeq[JsObject]): JsValue = {
    // Alternatives are added per user
    // So for each key we have a list of object containing the user id and the value
    val alternatives: Map[String, Seq[(String, JsValue)]] = manualUpdates
      .map { instance =>
        val dataMap: Map[String, JsValue] = instance
          .-("http://hbp.eu/manual#parent")
          .-("http://hbp.eu/manual#origin").as[Map[String, JsValue]]
        val userId: String = dataMap("http://hbp.eu/manual#updater_id").as[String]
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
        .-("http://hbp.eu/manual#update_timestamp")
        .-("http://hbp.eu/manual#updater_id")
    )
  }


}
