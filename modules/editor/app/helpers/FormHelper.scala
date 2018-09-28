
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

import common.models.{NexusInstance, NexusPath}
import play.Play
import play.api.libs.json._


object FormHelper {

  val slashEscaper = "%nexus-slash%"
  val formRegistry = loadFormConfiguration()
  val editableEntitiyTypes = buildEditableEntityTypesFromRegistry()


  def loadFormConfiguration() = {
    val confFile = Play.application().classloader.getResourceAsStream("editor_interface_configuration.json")
    try {
      Json.parse(confFile).as[JsObject]
    } catch {
      case _: Throwable => JsObject.empty
    } finally {
      confFile.close()
    }
  }

  def buildEditableEntityTypesFromRegistry(): JsObject = {
    val res = formRegistry.value.flatMap{
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
                    fieldContent.as[JsObject] + ("value", transformToArray(key, data, reconciledSuffix))
                  case _ =>
                    fieldContent.as[JsObject] + ("value", (data \ key).get)
                }

                filledTemplate + (escapeSlash(key), newValue)
              } else {
                filledTemplate + (escapeSlash(key), fieldContent.as[JsObject] )
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
              (escapeSlash(key) , value)
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

}
