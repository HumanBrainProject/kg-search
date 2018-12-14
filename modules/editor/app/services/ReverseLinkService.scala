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
import constants.EditorConstants.Command
import helpers.ReverseLinkOP
import helpers.ReverseLinkOP.removeReverseLinksFromInstance
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.specification.{FormRegistry, QuerySpec, UISpec}
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
        val (instanceWithoutReverseLinks, reverseEntitiesResponses) =
          processReverseLinks(
            updateToBeStored,
            instanceRef,
            formRegistry,
            queryRegistry,
            currentInstanceDisplayed,
            config.nexusEndpoint,
            user,
            token
          )
        reverseEntitiesResponses.flatMap { results =>
          if (results.forall(_.isRight)) {
            if (instanceWithoutReverseLinks.nexusInstance.content.keys.isEmpty) {
              Future(Right(()))
            } else {
              //Normal update of the instance without reverse links
              editorService.processInstanceUpdate(instanceRef, instanceWithoutReverseLinks, user, token)
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
    formRegistry: FormRegistry[UISpec],
    queryRegistry: FormRegistry[QuerySpec],
    currentInstanceDisplayed: NexusInstance,
    baseUrl: String,
    user: User,
    token: String
  ): (EditorInstance, Future[List[Either[APIEditorError, Unit]]]) = {
    val fields = formRegistry.registry(updateToBeStored.nexusInstance.nexusPath).fields
    val instanceWithoutReversLink = removeReverseLinksFromInstance(updateToBeStored, fields)
    val instances = for {
      reverseField <- updateToBeStored.contentToMap()
      if fields(reverseField._1).isReverse.getOrElse(false)
      reverseFieldPath <- fields(reverseField._1).instancesPath
      fieldName        <- fields(reverseField._1).reverseTargetField
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
      val changes = ReverseLinkOP.computeChanges(currentInstanceDisplayed, reverseField, fullIds.toList)
      changes.map { s =>
        processCommand(s._1, s._2, updateReference, queryRegistry, fieldName, baseUrl, user, token)
      }
    }
    (instanceWithoutReversLink, Future.sequence(instances.toList.flatten))
  }

  /**
    *  Process the command either ADD or DELETE
    * @param command either ADD or DELETE
    * @param reverseInstancelink the link to the reverseInstance
    * @param currentInstanceRef current instance reference
    * @param queryRegistry query spec registry
    * @param targetField the field name in the reverse instance pointing to the current instance or not
    * @param baseUrl env base url
    * @param user current user
    * @param token current user token
    * @return
    */
  private def processCommand(
    command: Command,
    reverseInstancelink: NexusLink,
    currentInstanceRef: NexusInstanceReference,
    queryRegistry: FormRegistry[QuerySpec],
    targetField: String,
    baseUrl: String,
    user: User,
    token: String
  ): Future[Either[APIEditorError, Unit]] = {
    editorService.retrieveInstance(reverseInstancelink.ref, token, queryRegistry).flatMap {
      case Left(error) => Future(Left(error))
      case Right(reverseInstance) =>
        val diffToSave =
          ReverseLinkOP
            .createContentToUpdate(command, reverseInstance, targetField, currentInstanceRef)
        diffToSave match {
          case Right(Some(l)) =>
            val reverseLinkInstance = EditorInstance(
              NexusInstance(
                Some(reverseInstancelink.ref.id),
                reverseInstancelink.ref.nexusPath,
                Json.obj(targetField -> Json.toJson(l.map(_.toJson(baseUrl))))
              )
            )
            editorService.updateInstance(reverseLinkInstance, reverseInstancelink.ref, token, user.id)
          case Right(None) => Future(Right(()))
          case Left(s)     => Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, s)))
        }
    }

  }

}
