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
import models._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.specification._
import models.user.NexusUser
import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services.{ConfigurationService, OIDCAuthService}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}

case class FormRegistries(formRegistry: FormRegistry[UISpec], queryRegistry: FormRegistry[QuerySpec])

@Singleton
class FormService @Inject()(
  config: ConfigurationService,
  ws: WSClient,
  OIDCAuthService: OIDCAuthService,
  specificationService: SpecificationService
)(implicit ec: ExecutionContext) {

  private var stateSpec: Option[FormRegistries] = None

  val timeout = FiniteDuration(20, "sec")
  val retryTime = 5000 //ms
  val logger = Logger(this.getClass)

  Await.result(flushSpec(), timeout)

  def getRegistries(): FormRegistries = stateSpec match {
    case Some(reg) => reg
    case None      => Await.result(loadFormConfiguration(), timeout).get
  }

  def flushSpec(): Future[Unit] =
    for {
      specInit   <- specificationService.init()
      reloadForm <- loadFormConfiguration()
    } yield ()

  final def loadFormConfiguration(): Future[Option[FormRegistries]] = {
    logger.info("Starting to load specification")
    for {
      token     <- OIDCAuthService.getTechAccessToken(true)
      querySpec <- ws.url(s"${config.kgQueryEndpoint}/arango/internalDocuments/editor_specifications").get()
    } yield {
      querySpec.status match {
        case OK =>
          val registries = FormService.getRegistry(querySpec.json.as[List[JsObject]], specificationService, token)
          stateSpec = Some(registries)
          logger.info("Specification loaded")
          Some(registries)
        case _ =>
          logger.warn(s"Could not load configuration")
          None
      }
    }
  }
}

object FormService {
  val log = LoggerFactory.getLogger(this.getClass)

  def removeKey(jsValue: JsValue): JsValue = {
    if (jsValue.validateOpt[JsObject].isSuccess) {
      if (jsValue.toString() == "null") {
        JsNull
      } else {
        val correctedObj = jsValue
          .as[JsObject] - "description" - "name" - "status" - "childrenStatus" - UiConstants.DATATYPE -
        s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}" - UiConstants.LABEL - UiConstants.SCHEMA
        val res = correctedObj.value.map {
          case (k, v) => k -> removeKey(v)
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
    registry.registry.toSeq.sortWith { case (jsO1, jsO2) => jsO1._1.toString() < jsO2._1.toString() }.toList
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

  /**
    * Building instance from a form sent by the UI
    * The Form will be filled with the values passed from the UI except for reverse links and linking instances
    *
    * @param instanceRef   The reference to the instance newly created
    * @param nexusEndpoint The nexus endpoint used to generate full urls
    * @param newInstance   The new instance which is empty as the instance is created empty then filled with data
    * @param registry      The registry to reflect on UISpec
    * @return A
    */
  def buildNewInstanceFromForm(
    instanceRef: NexusInstanceReference,
    nexusEndpoint: String,
    newInstance: JsObject,
    registry: FormRegistry[UISpec]
  ): EditorInstance = {

    def addNexusEndpointToLinks(item: JsValue): JsObject = {
      val id = (item.as[JsObject] \ "id").as[String]
      if (!id.startsWith("http://")) {
        Json.obj(JsonLDConstants.ID -> JsString(s"$nexusEndpoint/v0/data/$id"))
      } else {
        Json.obj(JsonLDConstants.ID -> JsString(id))
      }
    }

    val fields = registry.registry(instanceRef.nexusPath).getFieldsAsMap
    val m = newInstance.value
      .filterNot(
        s =>
          fields(s._1).isReverse.exists(identity) ||
          fields(s._1).isLinkingInstance.exists(identity) ||
          s._1 == InternalSchemaFieldsConstants.ALTERNATIVES
      )
      .map {
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
    EditorInstance(
      NexusInstance(
        Some(instanceRef.id),
        instanceRef.nexusPath,
        Json.toJson(m).as[JsObject]
      )
    )
  }

  def getFormStructure(entityType: NexusPath, data: JsValue, formRegistry: FormRegistry[UISpec]): JsValue = {
    // retrieve form template
    val formTemplateOpt = formRegistry.registry.get(entityType)
    formTemplateOpt match {
      case Some(formTemplate) =>
        if (data != JsNull) {
          // fill template with data
          val fields = fillFields(formTemplate, data, entityType)
          fillFormTemplate(
            fields,
            formTemplate,
            (data \ EditorInstance.Fields.alternatives).asOpt[JsObject].getOrElse(Json.obj())
          )
        } else {
          //Returning a blank template
          val escapedForm = formTemplate.getFieldsAsLinkedMap.map {
            case (key, value) =>
              (key, value)
          }
          fillFormTemplate(Json.toJson(escapedForm), formTemplate, Json.obj())
        }
      case None =>
        JsNull
    }
  }

  private def fillFields(formTemplate: UISpec, data: JsValue, entityType: NexusPath): JsObject = {
    val nexusId = (data \ s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}").as[String]
    val idFields = Json.obj(
      "id" -> Json.obj("value" -> Json.obj("path" -> entityType.toString()), "nexus_id" -> JsString(nexusId))
    )
    formTemplate.getFieldsAsLinkedMap.foldLeft(idFields) {
      case (filledTemplate, (id, fieldContent)) =>
        if (data.as[JsObject].keys.contains(id)) {
          val newValue = fieldContent.fieldType match {
            case DropdownSelect =>
              fieldContent.copy(value = Some(FormService.transformToArray(id, data)))
            case InputText | InputTextMultiple | TextArea =>
              val field = data \ id
              val textValue = if (field.validate[List[String]].isSuccess) {
                if (!field.as[List[String]].exists(j => j != null)) {
                  JsString("")
                } else {
                  JsString(field.as[List[String]].head)
                }
              } else if (field.as[JsValue] == JsNull) {
                JsString("")
              } else {
                field
              }
              fieldContent.copy(value = textValue.asOpt[JsValue])
            case _ =>
              fieldContent.copy(value = (data \ id).asOpt[JsValue])
          }
          val valueWithoutNull = newValue.value match {
            case Some(JsNull) => newValue.copy(value = None)
            case _            => newValue
          }
          filledTemplate ++ valueWithoutNull.transformToNormalizedJsonStructure()
        } else {
          filledTemplate ++ fieldContent.transformToNormalizedJsonStructure()
        }
    }
  }

  def fillFormTemplate(fields: JsValue, formTemplate: UISpec, alternatives: JsObject = Json.obj()): JsValue = {
    val alt = stripAlternativeVocab(alternatives)
    Json.obj("fields" -> fields) +
    ("label", JsString(formTemplate.label)) +
    ("editable", JsBoolean(formTemplate.isEditable.getOrElse(true))) +
    ("ui_info", formTemplate.uiInfo.map(Json.toJson(_)).getOrElse(Json.obj())) +
    ("alternatives", alt)
  }

  def stripAlternativeVocab(jsObject: JsObject): JsObject = {
    val m = jsObject.value.map {
      case (k, v) =>
        val array = if (v.validate[JsArray].isError) {
          JsArray().append(v)
        } else {
          v
        }
        k -> array
          .as[List[JsValue]]
          .filterNot(js => js.validate[String].isSuccess && js.as[String].startsWith("embedded/"))
          .map(
            js =>
              if (js.validate[JsObject].isSuccess) {
                Json.toJson(js.as[JsObject].value.map {
                  case (key, value) =>
                    val newValue = if (key == InternalSchemaFieldsConstants.ALTERNATIVES_USERIDS) {
                      if (value.validate[JsArray].isError) {
                        JsArray().append(value)
                      } else {
                        value
                      }
                    } else {
                      formatAlternativeLinks(value)
                    }
                    key.stripPrefix(InternalSchemaFieldsConstants.ALTERNATIVES + "/") -> newValue
                })
              } else {
                js
            }
          )
    }
    Json.toJson(m).as[JsObject]
  }

  def formatAlternativeLinks(jsValue: JsValue): JsValue = {
    jsValue match {
      case j if j.validate[JsArray].isSuccess =>
        Json.toJson(j.as[JsArray].value.map { js =>
          if (js.validate[JsObject].isSuccess) {
            if (js.as[JsObject].keys.contains(JsonLDConstants.ID)) {
              Json.obj("id" -> js.as[JsObject].value.get(SchemaFieldsConstants.RELATIVEURL))
            } else {
              js
            }
          } else {
            js
          }
        })
      case j if j.validate[JsObject].isSuccess =>
        if (j.as[JsObject].keys.contains(JsonLDConstants.ID)) {
          Json.obj("id" -> j.as[JsObject].value.get(SchemaFieldsConstants.RELATIVEURL))
        } else {
          j
        }
      case j => j
    }
  }

  def editableEntities(user: NexusUser, formRegistry: FormRegistry[UISpec]): List[(NexusPath, UISpec)] = {
    val registry = FormRegistry.filterOrgs(formRegistry, user.organizations)
    buildEditableEntityTypesFromRegistry(registry)
  }
  private val timeout = FiniteDuration(30, "sec")

  def getRegistry(
    js: List[JsObject],
    specificationService: SpecificationService,
    token: RefreshAccessToken
  ): FormRegistries = {
    val systemQueries = Await.result(specificationService.getOrCreateSpecificationQueries(token), timeout)
    val configQueries = extractToRegistry[QuerySpec](js, "query")
    val completeQueries = FormRegistry(
      systemQueries.foldLeft(configQueries.registry) {
        case (acc, el) => acc.updated(el.nexusPath, QuerySpec(Json.obj(), Some(el.id)))
      }
    )

    val uiSpecResponse = Await.result(specificationService.fetchSpecifications(token), timeout)
    val uiSpecFromDB = uiSpecResponse.status match {
      case OK => (uiSpecResponse.json \ "results").as[List[JsObject]]
      case _ =>
        log.error(
          s"Could not fetch specification from DB - Status: ${uiSpecResponse.status} Content: ${uiSpecResponse.body}"
        )
        List()
    }
    val registry = extractToRegistry[UISpec](js, "uiSpec")
    val uiSpecRegistry = registry.copy(
      registry = uiSpecFromDB.foldLeft(registry.registry) {
        case (acc, el) =>
          if ((el \ "targetType").asOpt[String].isDefined) {
            val path = NexusPath((el \ "targetType").as[String])
            acc.updated(path, el.as[UISpec])
          } else {
            acc
          }
      }
    )
    FormRegistries(uiSpecRegistry, completeQueries)
  }

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
              } yield NexusPath(org, domain, schema, version) -> v.as[A](r)
            }
            .getOrElse(List())
          acc ++ listOfMap
      }
    )
  }
}
