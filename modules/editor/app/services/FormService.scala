
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

import com.google.inject.{Inject, Singleton}
import constants.{EditorConstants, InternalSchemaFieldsConstants, JsonLDConstants, UiConstants}
import models.editorUserList.BookmarkList
import models._
import models.instance.{EditorInstance, NexusInstance}
import models.user.NexusUser
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.http.Status.OK

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}


@Singleton
class FormService @Inject()(
                             config: ConfigurationService,
                             ws: WSClient
                           )(implicit ec: ExecutionContext){

  lazy val formRegistry: FormRegistry = loadFormConfiguration()
  val timeout = FiniteDuration(15, "sec")
  val retryTime = 5000 //ms
  val logger = Logger(this.getClass)

  @tailrec
  final def loadFormConfiguration(): FormRegistry = {
    val spec = Await.result(
      ws.url(s"${config.kgQueryEndpoint}/arango/internalDocuments/editor_specifications").get(),
      timeout
    )
    spec.status match {
      case OK => FormRegistry(
        spec.json.as[List[JsObject]].foldLeft(Json.obj()) {
          case (acc, el) => acc ++ (el \ "uiSpec").as[JsObject]
        }
      )
      case _ =>
        logger.warn(s"Could not load configuration, retrying in ${retryTime / 1000} secs")
        Thread.sleep(retryTime)
        loadFormConfiguration()
    }

  }
}
object FormService{

  object FormRegistryService extends FormRegistryService

  def removeKey(jsValue: JsValue):JsValue = {
    if (jsValue.validateOpt[JsObject].isSuccess) {
      if(jsValue.toString() == "null"){
        JsNull
      }else{
        val correctedObj = jsValue.as[JsObject] - "description" - "name" - "status" - "childrenStatus" - UiConstants.DATATYPE -
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
    if ((data \ key ).validate[JsArray].isSuccess){
      Json.toJson((data \ key ).as[JsArray].value.map{ js =>
        if ((js \ InternalSchemaFieldsConstants.RELATIVEURL).isDefined) {
          val linkToInstance = (js \ InternalSchemaFieldsConstants.RELATIVEURL).as[String]
          Json.obj("id" -> JsString(linkToInstance))
        } else {
          js
        }
      }).as[JsArray]
    }else {
      if ((data \ key \ InternalSchemaFieldsConstants.RELATIVEURL).isDefined) {
        val linkToInstance = (data \ key \ InternalSchemaFieldsConstants.RELATIVEURL).as[String]
        JsArray().+:(Json.obj("id" -> JsString(linkToInstance)))
      } else {
        JsArray()
      }
    }
  }

  def buildEditableEntityTypesFromRegistry(registry: FormRegistry): List[BookmarkList] = {
    registry.registry.value.flatMap{
      case (organization, organizationDetails) =>
        organizationDetails.as[JsObject].value.flatMap{
          case (domain, domainDetails) =>
            domainDetails.as[JsObject].value.flatMap{
              case (schema, schemaDetails) =>
                schemaDetails.as[JsObject].value.map{
                  case (version, formDetails) =>
                    BookmarkList(
                      s"$organization/$domain/$schema/$version",
                      (formDetails.as[JsObject] \ UiConstants.LABEL).as[String],
                      Some((formDetails.as[JsObject] \ "editable").asOpt[Boolean].getOrElse(true)),
                      (formDetails.as[JsObject] \ "ui_info").asOpt[JsObject],
                      (formDetails.as[JsObject] \ "color").asOpt[String]
                    )
                }
            }
        }
    }.toSeq.sortWith{case (jsO1, jsO2) => jsO1.name < jsO2.name}.toList
  }

  def buildInstanceFromForm(original: NexusInstance, modificationFromUser: JsValue, nexusEndpoint: String): NexusInstance = {
    //    val flattened = JsFlattener(formContent)
    //    applyChanges(original, flattened)
    val formContent = Json.parse(modificationFromUser.toString()).as[JsObject] - "id"
    val formWithID = removeClientKeysCorrectLinks(formContent, nexusEndpoint)
//    val cleanForm = FormService.removeKey(formContent.as[JsValue])
//    val formWithID = cleanForm.toString().replaceAll(""""id":"""", s""""@id":"${nexusEndpoint}/v0/data/""")
    val res= original.content.deepMerge(formWithID)
    original.copy(content = res)
  }

  def removeClientKeysCorrectLinks(payload:JsValue, nexusEndpoint:String):JsObject = {
    val cleanForm = FormService.removeKey(payload)
    val form = cleanForm.toString().replaceAll(""""id":"""", s""""@id":"${nexusEndpoint}/v0/data/""")
    Json.parse(form).as[JsObject]
  }

  def isInSpec(id:String, registry: FormRegistry):Boolean = {
    val list = FormService.buildEditableEntityTypesFromRegistry(registry)
      .map(l => l.id)
    list.contains(id)
  }


  def buildNewInstanceFromForm(nexusEndpoint: String, instancePath: NexusPath, newInstance: JsObject, registry: FormRegistry): JsObject = {

    def addNexusEndpointToLinks(item: JsValue): JsObject = {
      val id = (item.as[JsObject] \ "id" ).as[String]
      if(!id.startsWith("http://")){
        Json.obj(JsonLDConstants.ID ->  JsString(s"$nexusEndpoint/v0/data/$id"))
      }else{
        Json.obj(JsonLDConstants.ID ->  JsString(id))
      }
    }

    val fields = (registry.registry \ instancePath.org \ instancePath.domain \ instancePath.schema \ instancePath.version \ UiConstants.FIELDS)
      .as[JsObject].value
    val m = newInstance.value.map{ case (key, v) =>
      val formObjectType = (fields(key) \ "type").as[String]
      formObjectType match {
        case "DropdownSelect" =>
          val arr: IndexedSeq[JsValue] = v.as[JsArray].value.map{ item =>
            addNexusEndpointToLinks(item)
          }
          key -> Json.toJson(arr)
        case _ =>
          if( (fields(key) \ "isLink").asOpt[Boolean].getOrElse(false)){
            key -> addNexusEndpointToLinks(v)
          } else{
            key -> v
          }
      }
    }
    Json.toJson(m).as[JsObject]
  }

  def getFormStructure(entityType: NexusPath, data: JsValue, formRegistry: FormRegistry): JsValue = {
    // retrieve form template
    val formTemplateOpt = (formRegistry.registry \ entityType.org \ entityType.domain \ entityType.schema \ entityType.version).asOpt[JsObject]

    formTemplateOpt match {
      case Some(formTemplate) =>
        if(data != JsNull){

          val nexusId = (data \ s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}").as[String]
          // fill template with data
          val idFields = Json.obj(
            "id" -> Json.obj(
              "value" -> Json.obj(
                "path" -> entityType.toString()),
              "nexus_id" -> JsString(nexusId))
          )

          val fields = (formTemplate \ UiConstants.FIELDS).as[JsObject].fields.foldLeft(idFields) {
            case (filledTemplate, (key, fieldContent)) =>
              if (data.as[JsObject].keys.contains(key)) {
                val newValue = (fieldContent \ "type").asOpt[String].getOrElse("") match {
                  case "DropdownSelect" =>
                    fieldContent.as[JsObject] + ("value", FormService.transformToArray(key, data))
                  case _ =>
                    fieldContent.as[JsObject] + ("value", (data \ key).get)
                }

                filledTemplate + (key, newValue)
              } else {
                filledTemplate + (key, fieldContent.as[JsObject] )
              }
          }
          fillFormTemplate(fields, formTemplate, (data \ EditorInstance.Fields.alternatives).asOpt[JsObject].getOrElse(Json.obj()) )

        }else {
          //Returning a blank template
          val escapedForm = ( formTemplate \ UiConstants.FIELDS).as[JsObject].value.map{
            case (key, value) =>
              (key , value)
          }
          fillFormTemplate(Json.toJson(escapedForm), formTemplate, Json.obj() )
        }
      case None =>
        JsNull
    }
  }


  def fillFormTemplate(fields: JsValue, formTemplate:JsValue, alternatives: JsObject = Json.obj()): JsValue ={
    Json.obj(UiConstants.FIELDS -> fields) +
      (UiConstants.LABEL, (formTemplate \ UiConstants.LABEL).get) +
      ("editable", JsBoolean((formTemplate.as[JsObject] \ "editable").asOpt[Boolean].getOrElse(true))) +
      ("ui_info", (formTemplate \ "ui_info").getOrElse(JsObject.empty)) +
      ("alternatives", alternatives )
  }

  def editableEntities(user: NexusUser, formRegistry: FormRegistry): List[BookmarkList] = {
    val registry = FormRegistryService.filterOrgs(formRegistry, user.organizations)
    buildEditableEntityTypesFromRegistry(registry)
  }




}
