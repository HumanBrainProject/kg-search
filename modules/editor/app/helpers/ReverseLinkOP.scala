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

import models.AccessToken
import models.commands.{AddReverseLinkCommand, Command, DeleteReverseLinkCommand}
import models.errors.APIEditorError
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.{EditorFieldSpecification, FormRegistry, QuerySpec}
import models.user.User
import monix.eval.Task
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.EditorService

object ReverseLinkOP {
  val log = Logger(this.getClass)

  def removeLinksFromInstance(
    instance: EditorInstance,
    instanceFieldsSpecs: Map[String, EditorFieldSpecification],
    predicate: EditorFieldSpecification => Boolean
  ): EditorInstance = instance.copy(
    nexusInstance = instance.nexusInstance.copy(
      content = Json
        .toJson(
          instance
            .contentToMap()
            .filterNot(
              k => predicate(instanceFieldsSpecs(k._1))
            )
        )
        .as[JsObject]
    )
  )

  def getAddedAndRemovedLinks(
    currentInstanceDisplayed: NexusInstance,
    linkName: String,
    ids: List[NexusLink]
  ): (List[NexusLink], List[NexusLink]) = {
    currentInstanceDisplayed.content.value.get(linkName) match {
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
              .toList
        }
        val removed = currentLinks.toSet -- ids.toSet
        val added = ids.toSet -- currentLinks.toSet
        (added.toList, removed.toList)
      case None => (ids, List())
    }
  }

  /**
    *   Add or delete a reverse link
    * @param currentInstanceDisplayed
    * @param linkName The name of the field in the current instance displayed
    * @param targetField The name of the field in the target instance
    * @param fullIds the list of ids of the target objects
    * @param editorService The editor service
    * @param token the user token
    * @param baseUrl base url of the system
    * @param user the current user
    * @param queryRegistry the registry containing queries
    * @return A list of commands to execute
    */
  def addOrDeleteReverseLink(
    currentInstanceDisplayed: NexusInstance,
    linkName: String,
    targetField: String,
    fullIds: List[NexusLink],
    editorService: EditorService,
    token: AccessToken,
    baseUrl: String,
    user: User,
    queryRegistry: FormRegistry[QuerySpec]
  ): List[Task[Either[APIEditorError, Command]]] = {
    val (added, removed) = getAddedAndRemovedLinks(currentInstanceDisplayed, linkName, fullIds)
    removed.map { link =>
      editorService.retrieveInstance(link.ref, token, queryRegistry).map {
        case Right(reverseInstance) =>
          Right(
            DeleteReverseLinkCommand(
              link,
              reverseInstance,
              targetField,
              NexusInstanceReference.fromUrl(currentInstanceDisplayed.id().get),
              editorService,
              baseUrl,
              token,
              user
            )
          )
        case Left(error) => Left(error)
      }
    } ::: added.map { link =>
      editorService.retrieveInstance(link.ref, token, queryRegistry).map {
        case Right(reverseInstance) =>
          Right(
            AddReverseLinkCommand(
              link,
              reverseInstance,
              targetField,
              NexusInstanceReference.fromUrl(currentInstanceDisplayed.id().get),
              editorService,
              baseUrl,
              token,
              user
            )
          )
        case Left(error) => Left(error)
      }
    }
  }
//
//  /**
//    *  Create the content to update the reverse link instance
//    * @param command Command to decide if we should add or remove the current instance from the reverse instance
//    * @param reverseInstance the reverse instance
//    * @param targetField the field from the reverse instance pointing to the current instance or not
//    * @param currentInstanceRef the current instance
//    * @return
//    */
//  def createContentToUpdate(
//    command: Command,
//    reverseInstance: NexusInstance,
//    targetField: String,
//    currentInstanceRef: NexusInstanceReference
//  ): Either[String, Option[List[NexusLink]]] = {
//    command match {
//      case ADD =>
//        reverseInstance.content.value.get(targetField) match {
//          case Some(fieldValue) =>
//            addLink(fieldValue, currentInstanceRef, targetField)
//          case None =>
//            //The field does not exists on the reverse instance we add it
//            Right(Some(List(NexusLink(currentInstanceRef))))
//        }
//      case DELETE =>
//        reverseInstance.content.value.get(targetField) match {
//          case Some(fieldValue) => removeLink(fieldValue, currentInstanceRef, targetField)
//          case None             =>
//            //The field does not exists we do nothing
//            Right(None)
//        }
//    }
//  }

  def addLink(
    fieldValue: JsValue,
    currentInstanceRef: NexusInstanceReference,
    targetField: String
  ): Either[String, List[NexusLink]] = {
    fieldValue match {
      case _ if fieldValue.validate[List[NexusLink]].isSuccess =>
        Right(NexusLink(currentInstanceRef) :: fieldValue.as[List[NexusLink]])
      case _ if fieldValue.validate[NexusLink].isSuccess =>
        Right(List(NexusLink(currentInstanceRef), fieldValue.as[NexusLink]))
      case l if l == JsObject.empty || l == JsArray.empty => Right(List(NexusLink(currentInstanceRef)))
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
