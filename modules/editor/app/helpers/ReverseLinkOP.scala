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

import constants.EditorConstants
import constants.EditorConstants.{ADD, Command, DELETE}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.EditorFieldSpecification
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

object ReverseLinkOP {
  type ReverseLinks = List[(EditorConstants.Command, EditorInstance, String)]
  val log = Logger(this.getClass)

  def removeReverseLinksFromInstance(
    instance: EditorInstance,
    instanceFieldsSpecs: Map[String, EditorFieldSpecification]
  ): EditorInstance = instance.copy(
    nexusInstance = instance.nexusInstance.copy(
      content = Json
        .toJson(
          instance
            .contentToMap()
            .filterNot(
              k =>
                instanceFieldsSpecs(k._1).isReverse
                  .getOrElse(false)
            )
        )
        .as[JsObject]
    )
  )

  /**
    * Generate either ADD or DELETE changes to apply on the reverse instance
    * @param currentInstanceDisplayed current instance
    * @param reverseField the reverse field
    * @param fullIds list of links  from the updated instance
    * @return
    */
  def computeChanges(
    currentInstanceDisplayed: NexusInstance,
    reverseField: (String, JsValue),
    fullIds: List[NexusLink]
  ): List[(EditorConstants.Command, NexusLink)] = {
    currentInstanceDisplayed.content.value.get(reverseField._1) match {
      case Some(currentReverseField) =>
        val currentLinks = currentReverseField.asOpt[JsObject] match {
          case Some(obj) =>
            List(NexusLink(NexusInstanceReference.fromUrl((obj.as[JsObject] \ "id").as[String])))
          case None =>
            currentReverseField
              .asOpt[JsArray]
              .map(
                _.value.map(js => NexusLink(NexusInstanceReference.fromUrl((js.as[JsObject] \ "id").as[String])))
              )
              .getOrElse(List())
        }
        val removed = currentLinks.toSet -- fullIds.toSet
        val added = fullIds.toSet -- currentLinks.toSet
        added.toList.map(s => (ADD, s)) ::: removed.toList.map(s => (DELETE, s))
      case None => // Add  all
        fullIds.map(s => (ADD, s))
    }
  }

  /**
    *  Create the content to update the reverse link instance
    * @param command Command to decide if we should add or remove the current instance from the reverse instance
    * @param reverseInstance the reverse instance
    * @param targetField the field from the reverse instance pointing to the current instance or not
    * @param currentInstanceRef the current instance
    * @return
    */
  def createContentToUpdate(
    command: Command,
    reverseInstance: NexusInstance,
    targetField: String,
    currentInstanceRef: NexusInstanceReference
  ): Either[String, Option[List[NexusLink]]] = {
    command match {
      case ADD =>
        reverseInstance.content.value.get(targetField) match {
          case Some(fieldValue) =>
            ReverseLinkOP.addLink(fieldValue, currentInstanceRef, targetField)
          case None =>
            //The field does not exists on the reverse instance we add it
            Right(Some(List(NexusLink(currentInstanceRef))))
        }
      case DELETE =>
        reverseInstance.content.value.get(targetField) match {
          case Some(fieldValue) => ReverseLinkOP.removeLink(fieldValue, currentInstanceRef, targetField)
          case None             =>
            //The field does not exists we do nothing
            Right(None)
        }
    }
  }

  def addLink(
    fieldValue: JsValue,
    currentInstanceRef: NexusInstanceReference,
    targetField: String
  ): Either[String, Some[List[NexusLink]]] = {
    fieldValue match {
      case _ if fieldValue.validate[List[NexusLink]].isSuccess =>
        Right(Some(NexusLink(currentInstanceRef) :: fieldValue.as[List[NexusLink]]))
      case _ if fieldValue.validate[NexusLink].isSuccess =>
        Right(Some(List(NexusLink(currentInstanceRef), fieldValue.as[NexusLink])))
      case l if l == JsObject.empty || l == JsArray.empty => Right(Some(List(NexusLink(currentInstanceRef))))
      case _                                              => Left("Cannot read reverse link type")
    }
  }

  def removeLink(
    fieldValue: JsValue,
    currentInstanceRef: NexusInstanceReference,
    targetField: String,
  ): Either[String, Some[List[NexusLink]]] = {
    fieldValue match {
      case _ if fieldValue.validate[List[NexusLink]].isSuccess =>
        Right(Some((fieldValue.as[List[NexusLink]].toSet - NexusLink(currentInstanceRef)).toList))
      case _ if fieldValue.validate[NexusLink].isSuccess =>
        Right(Some((Set(fieldValue.as[NexusLink]) - NexusLink(currentInstanceRef)).toList))
      case _ => Left("Cannot read reverse link type")
    }
  }
}
