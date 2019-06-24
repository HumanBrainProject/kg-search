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

import com.google.inject.Inject
import helpers.ReverseLinkOP
import models.{AccessToken, NexusPath}
import models.commands.{AddLinkingInstanceCommand, Command, DeleteLinkingInstanceCommand}
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.{EditorFieldSpecification, FormRegistry, QuerySpec, UISpec}
import models.user.User
import monix.eval.Task
import play.api.Logger
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.specification.{FormRegistries, FormService}

import scala.concurrent.{ExecutionContext, Future}

class ReverseLinkService @Inject()(
  editorService: EditorService,
  config: ConfigurationService,
  formService: FormService
) {

  val log = Logger(this.getClass)

  def generateDiffAndUpdateInstanceWithReverseLink(
    instanceRef: NexusInstanceReference,
    updateFromUser: JsValue,
    token: AccessToken,
    user: User,
    registries: FormRegistries
  ): Task[Either[APIEditorMultiError, Unit]] =
    editorService.retrieveInstance(instanceRef, token, registries.queryRegistry).flatMap {
      case Left(error) =>
        Task.pure(Left(APIEditorMultiError(error.status, List(error))))
      case Right(currentInstanceDisplayed) =>
        val updateToBeStored =
          EditorService.computeUpdateTobeStored(currentInstanceDisplayed, updateFromUser, config.nexusEndpoint)
        val fieldsSpec = registries.formRegistry.registry(updateToBeStored.nexusInstance.nexusPath).getFieldsAsMap
        val instanceWithoutReversLink = ReverseLinkOP.removeLinksFromInstance(
          updateToBeStored,
          fieldsSpec,
          e => e.isReverse.getOrElse(false)
        )
        val instanceWithoutLinkingInstance = ReverseLinkOP.removeLinksFromInstance(
          instanceWithoutReversLink,
          fieldsSpec,
          e => e.isLinkingInstance.getOrElse(false)
        )
        val reverseLinksToUpdated = updateReverseInstance(
          updateToBeStored,
          fieldsSpec,
          currentInstanceDisplayed,
          instanceRef,
          user,
          registries.queryRegistry,
          config.nexusEndpoint,
          token
        )
        val linkingInstancesToUpdated =
          updateLinkingInstances(updateToBeStored, fieldsSpec, currentInstanceDisplayed, instanceRef, Some(user), token)
        val responses = reverseLinksToUpdated.map { processCommand } ::: linkingInstancesToUpdated.map(_.execute())
        Task.gather(responses).flatMap { results =>
          if (results.forall(_.isRight)) {
            if (instanceWithoutLinkingInstance.nexusInstance.content.keys.isEmpty) {
              Task.pure(Right(()))
            } else {
              //Normal update of the instance without reverse links
              editorService.processInstanceUpdate(instanceRef, instanceWithoutLinkingInstance, user, token)
            }
          } else {
            val errors = results.filter(_.isLeft).map(_.swap.toOption.get)
            log.error(s"Errors while updating instance - ${errors.map(_.toJson.toString).mkString("\n")}")
            Task.pure(Left(APIEditorMultiError(errors.head.status, errors)))
          }
        }
    }

  def filterLinks(
    updateToBeStored: EditorInstance,
    fields: Map[String, EditorFieldSpecification],
    filter: EditorFieldSpecification => Boolean
  ): List[(String, List[NexusLink])] = {
    val instances = for {
      reverseField <- updateToBeStored.contentToMap()
      if filter(fields(reverseField._1))
    } yield {
      val fullIds = reverseField._2.asOpt[JsObject] match {
        case Some(obj) =>
          List(obj.as[NexusLink])
        case None =>
          reverseField._2
            .asOpt[JsArray]
            .map(
              _.value.map(_.as[NexusLink])
            )
            .getOrElse(List())
      }
      (reverseField._1, fullIds.toList)
    }
    instances.toList
  }

  def updateReverseInstance(
    updateToBeStored: EditorInstance,
    fieldsSpec: Map[String, EditorFieldSpecification],
    currentInstanceDisplayed: NexusInstance,
    instanceRef: NexusInstanceReference,
    user: User,
    queryRegistry: FormRegistry[QuerySpec],
    baseUrl: String,
    token: AccessToken
  ): List[Task[Either[APIEditorError, Command]]] = {
    filterLinks(
      updateToBeStored,
      fieldsSpec,
      e => e.isReverse.getOrElse(false)
    ).flatMap {
      case (reverseField, ids) =>
        fieldsSpec(reverseField).reverseTargetField match {
          case Some(fieldName) =>
            ReverseLinkOP
              .addOrDeleteReverseLink(
                currentInstanceDisplayed,
                reverseField,
                fieldName,
                ids,
                editorService,
                token,
                baseUrl,
                user,
                queryRegistry
              )
          case None => List()
        }
    }
  }

  /**
    *  Generate a list of Commands to add or delete a linking instance
    * @param updateToBeStored the current updated instance
    * @param fieldsSpec field specification
    * @param currentInstanceDisplayed the update before update
    * @param instanceRef the instance reference
    * @param token the user token
    * @return list of commands
    */
  def updateLinkingInstances(
    updateToBeStored: EditorInstance,
    fieldsSpec: Map[String, EditorFieldSpecification],
    currentInstanceDisplayed: NexusInstance,
    instanceRef: NexusInstanceReference,
    user: Option[User],
    token: AccessToken
  ): List[Command] = {
    filterLinks(
      updateToBeStored,
      fieldsSpec,
      e => e.isLinkingInstance.getOrElse(false)
    ).flatMap {
      case (reverseField, ids) =>
        val opt = for {
          linkingInstanceType    <- fieldsSpec(reverseField).linkingInstanceType
          linkingInstancePathStr <- fieldsSpec(reverseField).linkingInstancePath
        } yield {

          val linkingInstancePath = NexusPath(linkingInstancePathStr)
          val (added, removed) =
            ReverseLinkOP.getAddedAndRemovedLinks(currentInstanceDisplayed, reverseField, ids)
          added.map(
            id =>
              AddLinkingInstanceCommand(
                id,
                instanceRef,
                linkingInstanceType,
                linkingInstancePath,
                editorService,
                config.nexusEndpoint,
                user,
                token
            )
          ) ::: removed.map(
            id =>
              DeleteLinkingInstanceCommand(
                instanceRef,
                id.ref,
                linkingInstancePath,
                editorService,
                token
            )
          )
        }
        opt.getOrElse(List())
    }
  }

  /**
    *  Process the command either ADD or DELETE
    * @param c The command to execute
    * @return
    */
  private def processCommand(c: Task[Either[APIEditorError, Command]]): Task[Either[APIEditorError, Unit]] = {
    c.flatMap {
      case Left(err)      => Task.pure(Left(err))
      case Right(command) => command.execute()
    }
  }

}
