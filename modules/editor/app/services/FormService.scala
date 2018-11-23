
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
import constants.{EditorConstants, InternalSchemaFieldsConstants, NexusConstants}
import models.editorUserList.BookmarkList
import models._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.user.NexusUser
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}


@Singleton
class FormService @Inject()(
                             config: ConfigurationService,
                             ws: WSClient
                           )(implicit ec: ExecutionContext){

  lazy val formRegistry: FormRegistry = loadFormConfiguration()
  val timeout = FiniteDuration(15, "sec")

  def loadFormConfiguration(): FormRegistry = {
    val spec = Await.result(
      ws.url(s"${config.kgQueryEndpoint}/arango/internalDocuments/editor_specifications").get(),
      timeout
    )
    FormService.getRegistry(spec.json.as[List[JsObject]])
  }

}
object FormService{

  object FormRegistryService extends FormRegistryService

  def removeKey(jsValue: JsValue):JsValue = {
    if (jsValue.validateOpt[JsObject].isSuccess) {
      if(jsValue.toString() == "null"){
        JsNull
      }else{
        val correctedObj = jsValue.as[JsObject] - "description" - "name" - "status" - "childrenStatus" -
          s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}"
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
    registry.registry.map{
      case (path, formDetails) =>
        BookmarkList(
          path.toString(),
          formDetails.label,
          Some(formDetails.isEditable.getOrElse(true)),
          formDetails.uiInfo,
          formDetails.color
        )

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
        Json.obj("@id" ->  JsString(s"$nexusEndpoint/v0/data/$id"))
      }else{
        Json.obj("@id" ->  JsString(id))
      }
    }

    val fields = registry.registry(instancePath).fields
    val m = newInstance.value.map{ case (key, v) =>
      val formObjectType = fields(key).fieldType
      formObjectType match {
        case DropdownSelect =>
          val arr: IndexedSeq[JsValue] = v.as[JsArray].value.map{ item =>
            addNexusEndpointToLinks(item)
          }
          key -> Json.toJson(arr)
        case _ =>
          if( fields(key).isLink.getOrElse(false)){
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
    val formTemplateOpt = formRegistry.registry.get(entityType)

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

          val fields = formTemplate.fields.foldLeft(idFields) {
            case (filledTemplate, (id, fieldContent)) =>
              if (data.as[JsObject].keys.contains(id)) {
                val newValue = fieldContent.fieldType match {
                  case DropdownSelect =>
                    fieldContent.copy(value = Some(FormService.transformToArray(id, data)))
                  case _ =>
                    fieldContent.copy(value = (data \ id).asOpt[JsValue])
                }
                filledTemplate ++ Json.obj(id -> newValue)
              } else {
                filledTemplate ++ Json.obj(id -> fieldContent)
              }
          }
          fillFormTemplate(fields, formTemplate, (data \ EditorInstance.Fields.alternatives).asOpt[JsObject].getOrElse(Json.obj()) )

        }else {
          //Returning a blank template
          val escapedForm = formTemplate.fields.map{
            case (key, value) =>
              (key , value)
          }
          fillFormTemplate(Json.toJson(escapedForm), formTemplate, Json.obj() )
        }
      case None =>
        JsNull
    }
  }


  def fillFormTemplate(fields: JsValue, formTemplate:UISpec, alternatives: JsObject = Json.obj()): JsValue ={
    Json.obj("fields" -> fields) +
      ("label", JsString(formTemplate.label)) +
      ("editable", JsBoolean(formTemplate.isEditable.getOrElse(true))) +
      ("ui_info", formTemplate.uiInfo.map(Json.toJson(_)).getOrElse(Json.obj())) +
      ("alternatives", alternatives )
  }

  def editableEntities(user: NexusUser, formRegistry: FormRegistry): List[BookmarkList] = {
    val registry = FormRegistryService.filterOrgs(formRegistry, user.organizations)
    buildEditableEntityTypesFromRegistry(registry)
  }

  def getRegistry(js: List[JsObject]): FormRegistry = {
    FormRegistry(
      js.foldLeft(Map[NexusPath, UISpec]()) {
        case (acc, el) =>
            val listOfMap = (el \ "uiSpec").as[JsObject].value.flatMap {
              case (org, json) =>
                json.as[JsObject].value.flatMap{
                  case (domain, d) =>
                    d.as[JsObject].value.flatMap{
                      case (schema, s) =>
                        s.as[JsObject].value.map{
                          case (version, v) =>
                            NexusPath(org, domain, schema, version) -> v.as[UISpec]
                        }
                    }
                }
            }
          acc ++ listOfMap
      }
    )
  }

  def getReverseLinks(instance: EditorInstance, formRegistry: FormRegistry, originalInstance: NexusInstance, baseUrl: String):
  (EditorInstance, List[(EditorConstants.Command, EditorInstance, String)]) = {
    val fields = formRegistry.registry(instance.nexusInstance.nexusPath).fields
    val newContent = instance.copy(nexusInstance =
      instance.nexusInstance.copy(content = Json.toJson(instance.contentToMap().filterNot(k => fields(k._1).isReverse.getOrElse(false))).as[JsObject])
    )
    val instances = for {
      fieldMap <- instance.contentToMap() if fields(fieldMap._1).isReverse.getOrElse(false)
      path <- fields(fieldMap._1).instancesPath
      fieldName <- fields(fieldMap._1).reverseTargetField
      fullIds = fieldMap._2.as[JsArray].value.map(js => NexusInstanceReference.fromUrl((js \ "@id").as[String]))
    } yield {
      fullIds.foldLeft(List[(EditorConstants.Command, EditorInstance, String)]()) {
        case (updatesAndDeletes, ref) =>
          if (originalInstance.content.value(fieldMap._1).as[JsArray].value.exists(js => (js \ "@id").as[String].contains(ref.id))) {
            (EditorConstants.DELETE, EditorInstance(
              NexusInstance(
                Some(ref.id),
                NexusPath(path),
                Json.obj(
                  fieldName -> Json.obj("@id" -> JsString(s"$baseUrl/${NexusConstants.dataPath}${instance.nexusInstance.id().get}"))
                )
              )
            ), fieldMap._1) :: updatesAndDeletes
          } else {
            (EditorConstants.UPDATE, EditorInstance(
              NexusInstance(
                Some(ref.id),
                NexusPath(path),
                Json.obj(
                  fieldName -> Json.obj("@id" -> JsString(s"$baseUrl/${NexusConstants.dataPath}${instance.nexusInstance.id().get}"))
                )
              )
            ), fieldMap._1 ):: updatesAndDeletes
          }

      }
    }
    (newContent, instances.toList.flatten)
  }
}
