
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
import common.models.{NexusInstance, NexusPath, User}
import common.services.ConfigurationService
import org.json4s.native.JsonParser
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class FormService @Inject()(
                             config: ConfigurationService,
                             ws: WSClient
                           )(implicit ec: ExecutionContext){


  lazy val formRegistry = loadFormConfiguration()
//  private lazy val editableEntityTypes = buildEditableEntityTypesFromRegistry()
  val timeout = FiniteDuration(15, "sec")

  def loadFormConfiguration(): JsObject = {
    val spec = Await.result(
      ws.url(s"${config.kgQueryEndpoint}/arango/document/editor_specifications").get(),
      timeout
    )
    spec.json.as[List[JsObject]].foldLeft(Json.obj()) {
      case (acc, el) => acc ++ (el \ "uiSpec").as[JsObject]
    }
  }

  def editableEntities(user: User): JsValue = {
    val registry = this.formRegistry.value.filter{
      entity => user.groups.contains(s"nexus-${entity._1}")
    }
    FormService.buildEditableEntityTypesFromRegistry(Json.toJson(registry).as[JsObject])
  }


  def getFormStructure(entityType: NexusPath, data: JsValue, reconciledSuffix: String): JsValue = {
    // retrieve form template
    val formTemplateOpt = (formRegistry \ entityType.org \ entityType.domain \ entityType.schema \ entityType.version).asOpt[JsObject]

    formTemplateOpt match {
      case Some(formTemplate) =>
        if(data != JsNull){

          val nexusId = (data \ "@id").as[String]
          // fill template with data
          val idFields = Json.obj(
            ("id" -> Json.obj(
              ("value" -> Json.obj(
                ("path" -> entityType.toString()),
                ("nexus_id" -> JsString(nexusId))))))
          )

          val fields = (formTemplate \ "fields").as[JsObject].fields.foldLeft(idFields) {
            case (filledTemplate, (key, fieldContent)) =>
              if (data.as[JsObject].keys.contains(key)) {
                val newValue = (fieldContent \ "type").asOpt[String].getOrElse("") match {
                  case "DropdownSelect" =>
                    fieldContent.as[JsObject] + ("value", FormService.transformToArray(key, data, reconciledSuffix))
                  case _ =>
                    fieldContent.as[JsObject] + ("value", (data \ key).get)
                }

                filledTemplate + (FormService.escapeSlash(key), newValue)
              } else {
                filledTemplate + (FormService.escapeSlash(key), fieldContent.as[JsObject] )
              }
          }
          Json.obj("fields" -> fields) +
            ("label", (formTemplate \ "label").get) +
            ("editable", JsBoolean((formTemplate.as[JsObject] \ "editable").asOpt[Boolean].getOrElse(true))) +
            ("ui_info", (formTemplate \ "ui_info").getOrElse(JsObject.empty)) +
            ("alternatives", (data \ "http://hbp.eu/reconciled#alternatives").asOpt[JsObject].getOrElse(Json.obj()) )
         }else {
          //Returning a blank template
          val escapedForm = ( formTemplate \ "fields" ).as[JsObject].value.map{
            case (key, value) =>
              (FormService.escapeSlash(key) , value)
          }
          Json.obj("fields" -> Json.toJson(escapedForm)) +
            ("label", (formTemplate \ "label").get) +
            ("editable", JsBoolean((formTemplate.as[JsObject] \ "editable").asOpt[Boolean].getOrElse(true))) +
            ("ui_info", (formTemplate \ "ui_info").getOrElse(JsObject.empty)) +
            ("alternatives", Json.obj())
        }
      case None =>
        JsNull
    }
  }

  def buildNewInstanceFromForm(nexusEndpoint: String, instancePath: NexusPath, newInstance: JsObject): JsObject = {

    def addNexusEndpointToLinks(item: JsValue): JsObject = {
      val id = (item.as[JsObject] \ "id" ).as[String]
      if(!id.startsWith("http://")){
        Json.obj("@id" ->  JsString(s"$nexusEndpoint/v0/data/$id"))
      }else{
        Json.obj("@id" ->  JsString(id))
      }
    }

    val fields = (formRegistry \ instancePath.org \ instancePath.domain \ instancePath.schema \ instancePath.version \ "fields").as[JsObject].value
    val m = newInstance.value.map{ case (k, v) =>
      val key = FormService.unescapeSlash(k)
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


  def isInSpec(id:String):Boolean = {
    val list = (FormService.buildEditableEntityTypesFromRegistry(this.formRegistry) \ "data")
      .as[List[JsObject]]
      .map(js => (js  \ "path").as[String])
    list.contains(id)
  }

}
object FormService{

  val slashEscaper = "%nexus-slash%"

  def removeKey(jsValue: JsValue):JsValue = {
    if (jsValue.validateOpt[JsObject].isSuccess) {
      if(jsValue.toString() == "null"){
        JsNull
      }else{
        val correctedObj = jsValue.as[JsObject] - "description" - "label" - "status" - "childrenStatus"
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

  def escapeSlash(string: String): String = {
    string.replaceAll("/", slashEscaper)
  }

  def unescapeSlash(string: String): String = {
    string.replaceAll(slashEscaper, "/")
  }

  def transformToArray(key: String, data: JsValue, reconciledSuffix: String): JsArray = {
    if ((data \ key \ "@list").isDefined) {
      transformID((data \ key \ "@list").as[JsArray], reconciledSuffix)
    } else if ((data \ key ).validate[JsArray].isSuccess){
      transformID((data \ key ).as[JsArray], reconciledSuffix)
    }else {
      if ((data \ key \ "@id").isDefined) {
        val linkToInstance = (data \ key \ "@id").as[String]
        if (linkToInstance.contains("http")){
          val(id, path) = NexusInstance.extractIdAndPathFromString(linkToInstance)
          val instancePath = path.originalPath(reconciledSuffix)
          JsArray().+:(Json.obj("id" -> JsString(instancePath.toString() + s"/$id")))
        } else {
          JsArray()
        }
      } else {
        JsArray()
      }
    }
  }

  def transformID(jsArray: JsArray, reconciledSuffix: String):JsArray = {
    Json.toJson(
      jsArray.value.collect{
        case el if ((el \ "@id").as[String] contains "http") =>
          val(id, path) = NexusInstance.extractIdAndPathFromString((el \ "@id").as[String])
          val instancePath = path.originalPath(reconciledSuffix)
          Json.obj("id" -> JsString(instancePath.toString() + s"/$id"))
      }
    ).as[JsArray]
  }

  def buildEditableEntityTypesFromRegistry(registry: JsObject): JsObject = {
    val res = registry.value.flatMap{
      case (organization, organizationDetails) =>
        organizationDetails.as[JsObject].value.flatMap{
          case (domain, domainDetails) =>
            domainDetails.as[JsObject].value.flatMap{
              case (schema, schemaDetails) =>
                schemaDetails.as[JsObject].value.map{
                  case (version, formDetails) =>
                    Json.obj(
                      "path" -> JsString(s"$organization/$domain/$schema/$version"),
                      "label" -> (formDetails.as[JsObject] \ "label").get,
                      "editable" -> JsBoolean((formDetails.as[JsObject] \ "editable").asOpt[Boolean].getOrElse(true)),
                      "ui_info" -> (formDetails.as[JsObject] \ "ui_info").asOpt[JsObject].getOrElse[JsObject](Json.obj()) )
                }
            }
        }
    }.toSeq.sortWith{case (jsO1, jsO2) => (jsO1 \ "label").as[String] < ((jsO2 \ "label").as[String])}
    Json.obj("data" -> JsArray(res))
  }

  def buildInstanceFromForm(original: NexusInstance, modificationFromUser: JsValue, nexusEndpoint: String): NexusInstance = {
    //    val flattened = JsFlattener(formContent)
    //    applyChanges(original, flattened)
    val formContent = Json.parse(FormService.unescapeSlash(modificationFromUser.toString())).as[JsObject] - "id"
    val cleanForm = FormService.removeKey(formContent.as[JsValue])
    val formWithID = cleanForm.toString().replaceAll(""""id":"""", s""""@id":"${nexusEndpoint}/v0/data/""")
    val r = JsonParser.parse(original.content.toString()) merge(JsonParser.parse(formWithID))
    val res= original.content.deepMerge(Json.parse(formWithID).as[JsObject])
    original.copy(content = res)
  }


}
