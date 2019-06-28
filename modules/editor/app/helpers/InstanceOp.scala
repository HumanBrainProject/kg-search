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

import constants.{EditorConstants, JsonLDConstants, SchemaFieldsConstants}
import gnieh.diffson.playJson._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, PreviewInstance}
import models.specification.{DropdownSelect, FormRegistry, UISpec}
import play.api.Logger
import play.api.libs.json._
import services.specification.FormOp

object InstanceOp {
  val logger = Logger(this.getClass)

  type UpdateInfo = (Option[String], Int, String, EditorInstance)

  def buildDiffEntity(
    currentInstance: NexusInstance,
    newValue: NexusInstance,
  ): EditorInstance = {

    val patch: JsonMergePatch = JsonMergeDiff.diff(currentInstance.content, newValue.content)
    val deleted = patch.toJson
      .as[JsObject]
      .value
      .filter(s => s._2 == JsNull || (s._2.validate[JsArray].isSuccess && s._2.as[JsArray].value.isEmpty))
    val res: JsObject = patch(Json.obj()).as[JsObject]
    val withDeletedFields = res.deepMerge(Json.toJson(deleted).as[JsObject])
    val diffWithCompleteArray = withDeletedFields.value
      .map {
        case (k, v) =>
          val value = (newValue.content \ k).asOpt[JsValue]
          v match {
            case arr if arr.asOpt[JsArray].isDefined && value.isDefined => k -> value.get
            case _                                                      => k -> v
          }
      }
      .filter { // If the value in DB is null and the user sends en empty string we remove it from the diff
        case (k, v) =>
          if (v.asOpt[String].isDefined &&
              v.as[String].isEmpty &&
              currentInstance.content.value.get(k).isDefined &&
              currentInstance.content.value(k) == JsNull) {
            false
          } else {
            true
          }
      }
    EditorInstance(
      NexusInstance(
        currentInstance.nexusUUID,
        currentInstance.nexusPath,
        Json.toJson(diffWithCompleteArray).as[JsObject]
      )
    )
  }

  def md5HashString(s: String): String = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def formatInstanceList(
    jsArray: JsArray,
    dataType: String,
    formRegistry: FormRegistry[UISpec]
  ): List[PreviewInstance] = {

    jsArray.value.map { el =>
      val url = (el \ JsonLDConstants.ID).as[String]
      val name: String = (el \ SchemaFieldsConstants.NAME)
        .asOpt[String]
        .getOrElse(
          (el \ "http://hbp.eu/minds#alias")
            .asOpt[String]
            .getOrElse((el \ "http://hbp.eu/minds#title").asOpt[String].getOrElse(""))
        )
      val description: String = if ((el \ SchemaFieldsConstants.DESCRIPTION).isDefined) {
        (el \ SchemaFieldsConstants.DESCRIPTION).as[String]
      } else {
        ""
      }
      val id = url.split("/v0/data/").last
      val instanceRef = NexusInstanceReference.fromUrl(id)
      val label =
        formRegistry.registry.get(instanceRef.nexusPath).map(_.label).getOrElse(instanceRef.nexusPath.toString())
      PreviewInstance(instanceRef, name, dataType, Some(description), Some(label))
    }.toList
  }

  def addDefaultFields(instance: NexusInstance, formRegistry: FormRegistry[UISpec]): NexusInstance = {
    val m = formRegistry.registry(instance.nexusPath).getFieldsAsMap.map {
      case (k, v) =>
        val fieldValue = instance.getField(k)
        if (fieldValue.isEmpty) {
          v.fieldType match {
            case DropdownSelect =>
              k -> JsArray()
            case _ =>
              k -> JsNull
          }
        } else {
          k -> fieldValue.get
        }
    }
    val r = Json.toJson(m).as[JsObject].deepMerge(instance.content)
    instance.copy(content = Json.toJson(r).as[JsObject])
  }

  def removeEmptyFieldsNotInOriginal(originalInstance: NexusInstance, updates: EditorInstance): EditorInstance = {
    def compareUniqueElementObject(l: JsValue, r: JsValue): Boolean = {
      r.asOpt[List[JsObject]].isDefined && r.as[List[JsObject]].size == 1 &&
      l.asOpt[JsObject].isDefined &&
      r.as[List[JsObject]].head == l.as[JsObject]
    }

    val contentUpdate = updates.contentToMap().filter {
      case (k, v) =>
        if (((v.asOpt[JsArray].isDefined && v.as[List[JsValue]].isEmpty) ||
            v == JsNull) &&
            (originalInstance.content \ k).isEmpty) {
          false
        } else if (((originalInstance.content \ k).isDefined &&
                   compareUniqueElementObject(v, (originalInstance.content \ k).as[JsValue]))
                   ||
                   ((originalInstance.content \ k).isDefined &&
                   compareUniqueElementObject((originalInstance.content \ k).as[JsValue], v))) {
          // An object and an array with only this object should be considered the same
          false
        } else {
          true
        }
    }
    updates.copy(nexusInstance = updates.nexusInstance.copy(content = Json.toJson(contentUpdate).as[JsObject]))
  }

  def removeInternalFields(instance: NexusInstance): NexusInstance = {
    instance.copy(
      content = FormOp.removeKey(instance.content).as[JsObject] -
      s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}" -
      s"${EditorConstants.INFERENCESPACE}${EditorConstants.ALTERNATIVES}" -
      JsonLDConstants.ID - JsonLDConstants.TYPE - "https://schema.hbp.eu/inference/extends"
    )
  }

}
