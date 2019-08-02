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
package controllers

import com.google.inject.Inject
import constants.JsonLDConstants
import models.instance.{NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.{DropdownSelect, EditorFieldSpecification, GenericType, InputText, TextArea}
import models.{AuthenticatedUserAction, NexusPath}
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.ConfigurationService
import services.specification.{FormOp, FormService}

class SpecificationController @Inject()(
  cc: ControllerComponents,
  formService: FormService,
  authenticatedUserAction: AuthenticatedUserAction,
  configurationService: ConfigurationService
) extends AbstractController(cc) {

  implicit val scheduler = monix.execution.Scheduler.Implicits.global

  def getTemplate(org: String, domain: String, schema: String, version: String): Action[AnyContent] =
    authenticatedUserAction.async {
      formService
        .getRegistries()
        .map { registries =>
          val nexusPath = NexusPath(org, domain, schema, version)
          registries.formRegistry.registry.get(nexusPath).fold(NotFound("Could not find the requested specification")) {
            registry =>
              val template = registry.getFieldsAsMap.map {
                case (k, v) =>
                  k -> SpecificationController.getExampleValue(v, configurationService.nexusEndpoint)
              }
              Ok(
                Json.toJson(
                  template.updated(JsonLDConstants.TYPE, JsString(s"https://schema.hbp.eu/$org/${schema.capitalize}"))
                )
              )
          }
        }
        .runToFuture
    }

}

object SpecificationController {

  def getExampleValue(fieldSpec: EditorFieldSpecification, baseEndpoint: String): JsValue = {
    fieldSpec.fieldType match {
      case DropdownSelect if fieldSpec.isLink.exists(identity) =>
        val url = fieldSpec.instancesPath match {
          case Some(path) =>
            val p = NexusPath(path)
            s"$baseEndpoint/${NexusInstanceReference(p, "123").toString}"
          case None =>
            s"$baseEndpoint/${NexusInstanceReference("org", "domain", "schema", "version", "123").toString}"
        }
        Json.toJson(List(Json.obj(JsonLDConstants.ID -> url)))
      case DropdownSelect => Json.toJson(List("My value", "My other value"))
      case TextArea       => JsString("Large text area")
      case InputText      => JsString("Single line text input")
      case GenericType(_) if fieldSpec.isLinkingInstance.exists(identity) =>
        JsString(
          s"Reverse instance: this value should be set in the instance -> ${fieldSpec.instancesPath
            .fold("path/of/the/linkinginstance")(identity)}"
        )
      case GenericType(s) => JsString(s"No example available for this field of type $s")
    }
  }
}
