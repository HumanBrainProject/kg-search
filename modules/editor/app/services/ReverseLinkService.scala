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
import models.commands.Command
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.{EditorFieldSpecification, FormRegistry, QuerySpec, UISpec}
import models.user.User
import play.api.Logger
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.specification.FormService

import scala.concurrent.{ExecutionContext, Future}

class ReverseLinkService @Inject()(
  editorService: EditorService,
  config: ConfigurationService,
  formService: FormService
)(implicit executionContext: ExecutionContext) {

  val log = Logger(this.getClass)

  def generateDiffAndUpdateInstanceWithReverseLink(
    instanceRef: NexusInstanceReference,
    updateFromUser: JsValue,
    token: String,
    user: User,
    formRegistry: FormRegistry[UISpec],
    queryRegistry: FormRegistry[QuerySpec]
  ): Future[Either[APIEditorMultiError, Unit]] =
    editorService.retrieveInstance(instanceRef, token, queryRegistry).flatMap {
      case Left(error) =>
        Future(Left(APIEditorMultiError(error.status, List(error))))
      case Right(currentInstanceDisplayed) =>
        val updateToBeStored =
          EditorService.computeUpdateTobeStored(currentInstanceDisplayed, updateFromUser, config.nexusEndpoint)
        val fieldsSpec = formRegistry.registry(updateToBeStored.nexusInstance.nexusPath).fields
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
        val reverseEntitiesResponses =
          processReverseLinks(
            updateToBeStored,
            instanceRef,
            fieldsSpec,
            queryRegistry,
            currentInstanceDisplayed,
            config.nexusEndpoint,
            user,
            token
          )
        reverseEntitiesResponses.flatMap { results =>
          if (results.forall(_.isRight)) {
            if (instanceWithoutLinkingInstance.nexusInstance.content.keys.isEmpty) {
              Future(Right(()))
            } else {
              //Normal update of the instance without reverse links
              editorService.processInstanceUpdate(instanceRef, instanceWithoutLinkingInstance, user, token)
            }
          } else {
            val errors = results.filter(_.isLeft).map(_.swap.toOption.get)
            log.error(s"Errors while updating instance - ${errors.map(_.toJson.toString).mkString("\n")}")
            Future(Left(APIEditorMultiError(INTERNAL_SERVER_ERROR, errors)))
          }
        }
    }

  def processReverseLinks(
    updateToBeStored: EditorInstance,
    updateReference: NexusInstanceReference,
    fields: Map[String, EditorFieldSpecification],
    queryRegistry: FormRegistry[QuerySpec],
    currentInstanceDisplayed: NexusInstance,
    baseUrl: String,
    user: User,
    token: String
  ): Future[List[Either[APIEditorError, Unit]]] = {
    val instances = for {
      reverseField <- updateToBeStored.contentToMap()
      if fields(reverseField._1).isReverse.getOrElse(false)
      fieldName <- fields(reverseField._1).reverseTargetField
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
      val changes = ReverseLinkOP.computeChanges(
        currentInstanceDisplayed,
        reverseField,
        fieldName,
        fullIds.toList,
        editorService,
        token,
        baseUrl,
        user,
        queryRegistry
      )
      changes.map { processCommand }
    }
    Future.sequence(instances.toList.flatten)
  }

  /**
    *  Process the command either ADD or DELETE
    * @param c The command to execute
    * @return
    */
  private def processCommand(c: Future[Either[APIEditorError, Command]]): Future[Either[APIEditorError, Unit]] = {
    c.flatMap {
      case Left(err)      => Future(Left(err))
      case Right(command) => command.execute()
    }
  }

}
