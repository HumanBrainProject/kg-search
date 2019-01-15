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

package services.specification

import com.google.inject.{Inject, Singleton}
import constants._
import models.editorUserList.BookmarkList
import models.instance.{EditorInstance, NexusInstance}
import models.specification.{DropdownSelect, FormRegistry, QuerySpec, UISpec}
import models.user.NexusUser
import models.{specification, _}
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.{specification, ConfigurationService}

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class FormService @Inject()(
  config: ConfigurationService,
  ws: WSClient
)(implicit ec: ExecutionContext) {

  private var stateSpec: Option[(FormRegistry[UISpec], FormRegistry[QuerySpec])] = None

  val timeout = FiniteDuration(15, "sec")
  val retryTime = 5000 //ms
  val logger = Logger(this.getClass)

  def formRegistry: FormRegistry[UISpec] = loadFormConfiguration()._1
  def queryRegistry: FormRegistry[QuerySpec] = loadFormConfiguration()._2

  def flushSpec(): Unit = stateSpec = None

  @tailrec
  final def loadFormConfiguration(): (FormRegistry[UISpec], FormRegistry[QuerySpec]) = {
    stateSpec match {
      case None =>
        logger.info("Starting to load specification")
        val spec = Await.result(
          ws.url(s"${config.kgQueryEndpoint}/arango/internalDocuments/editor_specifications").get(),
          timeout
        )
        spec.status match {
          case OK =>
            logger.info("Specification loaded")
            stateSpec = Some(FormService.getRegistry(spec.json.as[List[JsObject]]))
            FormService.getRegistry(spec.json.as[List[JsObject]])
          case _ =>
            logger.warn(s"Could not load configuration, retrying in ${retryTime / 1000} secs")
            Thread.sleep(retryTime)
            loadFormConfiguration()
        }
      case Some(s) => s
    }
  }
}

object FormService {

  def removeKey(jsValue: JsValue): JsValue = {
    if (jsValue.validateOpt[JsObject].isSuccess) {
      if (jsValue.toString() == "null") {
        JsNull
      } else {
        val correctedObj = jsValue
          .as[JsObject] - "description" - "name" - "status" - "childrenStatus" - UiConstants.DATATYPE -
        s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}" - UiConstants.LABEL
        val res = correctedObj.value.map {
          case (k, v) =>
            k -> removeKey(v)
        }
        Json.toJson(res)
      }
    } else if (jsValue.validateOpt[JsArray].isSuccess) {
      Json.toJson(jsValue.as[JsArray].value.map(removeKey))
    } else {
      jsValue
    }
  }

  def transformToArray(key: String, data: JsValue): JsArray = {
    if ((data \ key).validate[JsArray].isSuccess) {
      Json
        .toJson((data \ key).as[JsArray].value.map { js =>
          if ((js \ SchemaFieldsConstants.RELATIVEURL).isDefined) {
            val linkToInstance = (js \ SchemaFieldsConstants.RELATIVEURL).as[String]
            Json.obj("id" -> JsString(linkToInstance))
          } else {
            js
          }
        })
        .as[JsArray]
    } else {
      if ((data \ key \ SchemaFieldsConstants.RELATIVEURL).isDefined) {
        val linkToInstance = (data \ key \ SchemaFieldsConstants.RELATIVEURL).as[String]
        JsArray().+:(Json.obj("id" -> JsString(linkToInstance)))
      } else {
        JsArray()
      }
    }
  }

  def buildEditableEntityTypesFromRegistry(registry: FormRegistry[UISpec]): List[(NexusPath, UISpec)] = {
    registry.registry
      .map {
        case (path, formDetails) =>
          (path, formDetails)
//          BookmarkList(
//            path.toString(),
//            formDetails.label,
//            Some(formDetails.isEditable.getOrElse(true)),
//            formDetails.uiInfo,
//            formDetails.color,
//            formDetails.folderID,
//            formDetails.folderName
//          )

      }
      .toSeq
      .sortWith { case (jsO1, jsO2) => jsO1._1.toString() < jsO2._1.toString() }
      .toList
  }

  def buildInstanceFromForm(
    original: NexusInstance,
    modificationFromUser: JsValue,
    nexusEndpoint: String
  ): NexusInstance = {
    val formContent = Json.parse(modificationFromUser.toString()).as[JsObject] - "id"
    val formWithID = removeClientKeysCorrectLinks(formContent, nexusEndpoint)
    val res = original.content.deepMerge(formWithID)
    original.copy(content = res)
  }

  def removeClientKeysCorrectLinks(payload: JsValue, nexusEndpoint: String): JsObject = {
    val cleanForm = FormService.removeKey(payload)
    val form = cleanForm.toString().replaceAll(""""id":"""", s""""@id":"$nexusEndpoint/v0/data/""")
    Json.parse(form).as[JsObject]
  }

  def isInSpec(id: String, registry: FormRegistry[UISpec]): Boolean = {
    val list = FormService
      .buildEditableEntityTypesFromRegistry(registry)
      .map(l => l._1.toString())
    list.contains(id)
  }

  def buildNewInstanceFromForm(
    nexusEndpoint: String,
    instancePath: NexusPath,
    newInstance: JsObject,
    registry: FormRegistry[UISpec]
  ): JsObject = {

    def addNexusEndpointToLinks(item: JsValue): JsObject = {
      val id = (item.as[JsObject] \ "id").as[String]
      if (!id.startsWith("http://")) {
        Json.obj(JsonLDConstants.ID -> JsString(s"$nexusEndpoint/v0/data/$id"))
      } else {
        Json.obj(JsonLDConstants.ID -> JsString(id))
      }
    }

    val fields = registry.registry(instancePath).fields
    val m = newInstance.value.map {
      case (key, v) =>
        val formObjectType = fields(key).fieldType
        formObjectType match {
          case DropdownSelect =>
            val arr: IndexedSeq[JsValue] = v.as[JsArray].value.map { item =>
              addNexusEndpointToLinks(item)
            }
            key -> Json.toJson(arr)
          case _ =>
            if (fields(key).isLink.getOrElse(false)) {
              key -> addNexusEndpointToLinks(v)
            } else {
              key -> v
            }
        }
    }
    Json.toJson(m).as[JsObject]
  }

  def getFormStructure(entityType: NexusPath, data: JsValue, formRegistry: FormRegistry[UISpec]): JsValue = {
    // retrieve form template
    val formTemplateOpt = formRegistry.registry.get(entityType)
    formTemplateOpt match {
      case Some(formTemplate) =>
        if (data != JsNull) {
          val nexusId = (data \ s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}").as[String]
          // fill template with data
          val idFields = Json.obj(
            "id" -> Json.obj("value" -> Json.obj("path" -> entityType.toString()), "nexus_id" -> JsString(nexusId))
          )
          val fields = formTemplate.fields.foldLeft(idFields) {
            case (filledTemplate, (id, fieldContent)) =>
              if (data.as[JsObject].keys.contains(id)) {
                val newValue = fieldContent.fieldType match {
                  case DropdownSelect =>
                    fieldContent.copy(value = Some(FormService.transformToArray(id, data)))
                  case _ =>
                    (data \ id).asOpt[JsValue] match {
                      case Some(null) => fieldContent.copy(value = None)
                      case s          => fieldContent.copy(value = s)
                    }
                }
                filledTemplate ++ Json.obj(id -> newValue)
              } else {
                filledTemplate ++ Json.obj(id -> fieldContent)
              }
          }
          fillFormTemplate(
            fields,
            formTemplate,
            (data \ EditorInstance.Fields.alternatives).asOpt[JsObject].getOrElse(Json.obj())
          )
        } else {
          //Returning a blank template
          fillFormTemplate(Json.toJson(formTemplate), formTemplate, Json.obj())
        }
      case None =>
        JsNull
    }
  }

  def fillFormTemplate(fields: JsValue, formTemplate: UISpec, alternatives: JsObject = Json.obj()): JsValue = {
    Json.obj("fields" -> fields) +
    ("label", JsString(formTemplate.label)) +
    ("editable", JsBoolean(formTemplate.isEditable.getOrElse(true))) +
    ("ui_info", formTemplate.uiInfo.map(Json.toJson(_)).getOrElse(Json.obj())) +
    ("alternatives", alternatives)
  }

  def editableEntities(user: NexusUser, formRegistry: FormRegistry[UISpec]): List[(NexusPath, UISpec)] = {
    val registry = FormRegistry.filterOrgs(formRegistry, user.organizations)
    buildEditableEntityTypesFromRegistry(registry)
  }

  def getRegistry(js: List[JsObject]): (FormRegistry[UISpec], FormRegistry[QuerySpec]) =
    (extractToRegistry[UISpec](js, "uiSpec"), extractToRegistry[QuerySpec](js, "query"))

  private def extractToRegistry[A](js: List[JsObject], field: String)(implicit r: Reads[A]): FormRegistry[A] = {
    FormRegistry(
      js.foldLeft(Map[NexusPath, A]()) {
        case (acc, el) =>
          val listOfMap = (el \ field)
            .asOpt[JsObject]
            .map { f =>
              for {
                (org, json)  <- f.value
                (domain, d)  <- json.as[JsObject].value
                (schema, s)  <- d.as[JsObject].value
                (version, v) <- s.as[JsObject].value
              } yield {
                NexusPath(org, domain, schema, version) -> v.as[A](r)
              }
            }
            .getOrElse(List())
          acc ++ listOfMap
      }
    )
  }
}
