
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

import constants.EditorConstants.{DELETE, UPDATE}
import constants.{EditorConstants, NexusConstants}
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.{FormRegistry, NexusPath}
import play.api.Logger
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

import scala.concurrent.Future

trait ReverseLinkService {
  val logger = Logger(this.getClass)

  private def isRefInOriginalInstanceField(instance: NexusInstance, fieldKey: String, ref: NexusInstanceReference): Boolean = {
    instance.content.value.get(fieldKey).isDefined &&
      instance.content.value(fieldKey).asOpt[JsArray].map(_.value.exists(js => (js \ "@id").as[String].contains(ref.id))).getOrElse(
        instance.content.value(fieldKey).asOpt[JsObject].exists(js =>  (js \ "@id").as[String].contains(ref.id)) )

  }
  type ReverseLinks = (EditorInstance, List[(EditorConstants.Command, EditorInstance, String)])
  def getReverseLinks(updateToBeStored: EditorInstance, formRegistry: FormRegistry, currentInstanceDisplayed: NexusInstance, baseUrl: String):
  ReverseLinks = {
    val fields = formRegistry.registry(updateToBeStored.nexusInstance.nexusPath).fields
    val instanceWithoutReversLink = updateToBeStored.copy(nexusInstance =
      updateToBeStored.nexusInstance.copy(content =
        Json.toJson(updateToBeStored.contentToMap().filterNot(k => fields(k._1).isReverse
          .getOrElse(false))).as[JsObject])
    )
    val instances = for {
      reverseFields <- updateToBeStored.contentToMap() if fields(reverseFields._1).isReverse.getOrElse(false)
      reverseFieldPath <- fields(reverseFields._1).instancesPath
      fieldName <- fields(reverseFields._1).reverseTargetField
    } yield {
      val fullIds = reverseFields._2.asOpt[JsObject] match {
        case Some(obj) => List(NexusInstanceReference.fromUrl((obj \ "@id").as[String]))
        case None => reverseFields._2.asOpt[JsArray].map(
          _.value.map(js => NexusInstanceReference.fromUrl((js \ "@id").as[String]))
        ).getOrElse(List())
      }
      fullIds.foldLeft(List[(EditorConstants.Command, EditorInstance, String)]()) {
        case (updatesAndDeletes, ref) =>
          val reversePath = NexusPath(reverseFieldPath)
          if (isRefInOriginalInstanceField(currentInstanceDisplayed, reverseFields._1, ref)) {
            (EditorConstants.DELETE, EditorInstance(
              NexusInstance(
                Some(ref.id),
                reversePath,
                Json.obj(
                  fieldName -> Json.obj("@id" -> JsString(s"$baseUrl/${NexusConstants.dataPath}${updateToBeStored.nexusInstance.id().get}"))
                )
              )
            ), fieldName) :: updatesAndDeletes
          } else {
            val reverseLinkInstance = handleUpdateOfReverseLink(baseUrl, fieldName,
              updateToBeStored, currentInstanceDisplayed, reverseFields._1, ref, reversePath)
            (EditorConstants.UPDATE, reverseLinkInstance, fieldName ):: updatesAndDeletes
          }
      }
    }
    (instanceWithoutReversLink, instances.toList.flatten)
  }

  private def handleUpdateOfReverseLink(baseUrl:String, fieldName:String, updateToBeStored:EditorInstance,
                                        originalInstance:NexusInstance, fieldKey: String, reference: NexusInstanceReference, path: NexusPath) = {
    val currentValue = originalInstance.content.value.get(fieldKey)
    val content = if( currentValue.isDefined && currentValue.get.asOpt[JsArray].isDefined){
      //TODO manage the array
      Json.arr()
    }else {
      Json.obj("@id" -> JsString(s"$baseUrl/${NexusConstants.dataPath}${updateToBeStored.nexusInstance.id().get}"))
    }
    EditorInstance(
      NexusInstance(
        Some(reference.id),
        path,
        Json.obj(
          fieldName -> content
        )
      )
    )
  }
}
