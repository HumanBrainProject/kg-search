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
import models.NexusPath
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.specification.{FormRegistry, QuerySpec, UISpec}
import models.user.User
import play.api.Logger
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.ReverseLinkOP.{handleUpdateOfReverseLink, isRefInOriginalInstanceField, removeReverseLinksFromInstance}
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
      reverseFields <- updateToBeStored.contentToMap()
      if fields(reverseFields._1).isReverse.getOrElse(false)
      reverseFieldPath <- fields(reverseFields._1).instancesPath
      fieldName        <- fields(reverseFields._1).reverseTargetField
    } yield {
      val fullIds = reverseFields._2.asOpt[JsObject] match {
        case Some(obj) =>
          List(NexusInstanceReference.fromUrl((obj \ "@id").as[String]))
        case None =>
          reverseFields._2
            .asOpt[JsArray]
            .map(
              _.value.map(js => NexusInstanceReference.fromUrl((js \ "@id").as[String]))
            )
            .getOrElse(List())
      }
      fullIds.map { ref =>
        val reversePath = NexusPath(reverseFieldPath)
        if (isRefInOriginalInstanceField(currentInstanceDisplayed, reverseFields._1, ref)) {
          log.debug(s"Reverse entities remove link ${ref.toString}")
          removeLinkFromInstance(
            ref,
            updateReference,
            fieldName,
            token,
            user.id
          )
        } else {
          log.debug(s"Reverse entities update ${ref.toString}")
          editorService.retrieveInstance(ref, token, queryRegistry).flatMap[Either[APIEditorError, Unit]] {
            case Left(e) => Future(Left(e))
            case Right(instance) =>
              val reverseLinkInstance = handleUpdateOfReverseLink(
                baseUrl,
                fieldName,
                updateToBeStored,
                updateReference,
                instance,
                reverseFields._1,
                ref,
                reversePath
              )
              editorService.updateInstance(reverseLinkInstance, ref, token, user.id)
          }
        }
      }
    }
    (instanceWithoutReversLink, Future.sequence(instances.toList.flatten))
  }

  private def removeLinkFromInstance(
    refInstanceToUpdate: NexusInstanceReference,
    instanceIdToRemove: NexusInstanceReference,
    targetField: String,
    token: String,
    userID: String
  ) =
    editorService.retrieveInstance(refInstanceToUpdate, token, formService.queryRegistry).flatMap {
      case Left(error) => Future(Left(error))
      case Right(instance) =>
        val content = instance.content.value(targetField)
        val correctedContent: Either[String, Option[JsObject]] =
          if (content.asOpt[JsArray].isDefined) {
            val c = content
              .as[JsArray]
              .value
              .filter(js => (js \ "@id").as[String].contains(instanceIdToRemove.toString))
            if (c.isEmpty) Right(None) else Right(Some(Json.obj(targetField -> c)))
          } else if (content.asOpt[JsObject].isDefined) {
            (content \ "@id")
              .asOpt[String]
              .map(
                s =>
                  if (s.contains(instanceIdToRemove.toString)) {
                    Right(Some(Json.obj(targetField -> Json.obj())))
                  } else {
                    Right(None)
                }
              )
              .getOrElse(Left(s"Could not process content of ${refInstanceToUpdate.id} - ${content.toString()}"))
          } else {
            Left(s"Could not process content of ${refInstanceToUpdate.id} - ${content.toString()}")
          }
        correctedContent match {
          case Right(Some(c)) =>
            editorService.updateInstance(EditorInstance(instance.copy(content = c)), refInstanceToUpdate, token, userID)
          case Right(None) => Future(Right(()))
          case Left(e) =>
            log.error(e)
            Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, e)))
        }
    }
}
