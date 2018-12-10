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
import constants.{EditorClient, EditorConstants, JsonLDConstants, UiConstants}
import constants.EditorConstants.{DELETE, UPDATE}
import helpers._
import models.errors.APIEditorError
import play.api.http.Status._
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, PreviewInstance}
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.user.User
import models.NexusPath
import models.specification.FormRegistry
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import services.ReverseLinkOP.{
  handleUpdateOfReverseLink,
  isRefInOriginalInstanceField,
  removeReverseLinksFromInstance,
  ReverseLinks
}
import services.instance.InstanceApiService
import services.query.QueryService
import services.specification.FormService

import scala.concurrent.{ExecutionContext, Future}

class EditorService @Inject()(
  wSClient: WSClient,
  config: ConfigurationService,
  formService: FormService
)(implicit executionContext: ExecutionContext) {

  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService

  object queryService extends QueryService

  def insertInstance(
    newInstance: NexusInstance,
    token: String
  ): Future[Either[APIEditorError, NexusInstanceReference]] = {
    val modifiedContent =
      FormService.removeClientKeysCorrectLinks(newInstance.content.as[JsValue], config.nexusEndpoint)
    instanceApiService
      .post(wSClient, config.kgQueryEndpoint, newInstance.copy(content = modifiedContent), token)
      .map {
        case Right(ref) => Right(ref)
        case Left(res)  => Left(APIEditorError(res.status, res.body))
      }
  }

  /**
    * Updating an instance
    *
    * @param diffInstance           The diff of the current instance and its modification
    * @param nexusInstanceReference The reference of the instance to update
    * @param token                  the user token
    * @param userId                 the id of the user sending the update
    * @return The updated instance
    */
  def updateInstance(
    diffInstance: EditorInstance,
    nexusInstanceReference: NexusInstanceReference,
    token: String,
    userId: String
  ): Future[Either[APIEditorError, Unit]] = {
    instanceApiService
      .put(wSClient, config.kgQueryEndpoint, nexusInstanceReference, diffInstance, token, userId)
      .map {
        case Left(res) => Left(APIEditorError(res.status, res.body))
        case Right(()) => Right(())
      }
  }

  /**
    * Return a instance by its nexus ID
    * Starting by checking if this instance is coming from a reconciled space.
    * Otherwise we try to return the instance from the original organization
    *
    * @param nexusInstanceReference The reference to the instace to retrieve
    * @param token                  The user access token
    * @return An error response or an the instance
    */
  def retrieveInstance(
    nexusInstanceReference: NexusInstanceReference,
    token: String
  ): Future[Either[APIEditorError, NexusInstance]] = {
    instanceApiService
      .get(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
      .map {
        case Left(res)       => Left(APIEditorError(res.status, res.body))
        case Right(instance) => Right(instance)
      }
  }

  def generateDiffAndUpdateInstance(
    instanceRef: NexusInstanceReference,
    updateFromUser: JsValue,
    token: String,
    user: User,
    formRegistry: FormRegistry
  ): Future[Either[APIEditorMultiError, Unit]] = retrieveInstance(instanceRef, token).flatMap {
    case Left(error) =>
      Future(Left(APIEditorMultiError(error.status, List(error))))
    case Right(currentInstanceDisplayed) =>
      val cleanedOriginalInstance =
        InstanceHelper.removeInternalFields(currentInstanceDisplayed)
      val instanceUpdateFromUser =
        FormService.buildInstanceFromForm(cleanedOriginalInstance, updateFromUser, config.nexusEndpoint)
      val diff = InstanceHelper.buildDiffEntity(cleanedOriginalInstance, instanceUpdateFromUser)
      val updateToBeStored =
        InstanceHelper.removeEmptyFieldsNotInOriginal(cleanedOriginalInstance, diff)
      val (instanceWithoutReverseLinks, reverseEntitiesResponses) =
        processReverseLinks(
          updateToBeStored,
          instanceRef,
          formRegistry,
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
            processInstanceUpdate(instanceRef, updateToBeStored, user, token)
          }
        } else {
          val errors = results.filter(_.isLeft).map(_.swap.toOption.get)
          logger.error(s"Errors while updating instance - ${errors.map(_.toJson.toString).mkString("\n")}")
          Future(Left(APIEditorMultiError(INTERNAL_SERVER_ERROR, errors)))
        }
      }
  }

  /**
    *  Update the reverse instance
    * @param instanceRef The reference to the instance being updated
    * @param updateToBeStored The instance being updated
    * @param user
    * @param token
    * @return
    */
  private def processInstanceUpdate(
    instanceRef: NexusInstanceReference,
    updateToBeStored: EditorInstance,
    user: User,
    token: String
  ): Future[Either[APIEditorMultiError, Unit]] =
    instanceApiService
      .get(wSClient, config.kgQueryEndpoint, instanceRef, token, EditorClient, Some(user.id))
      .flatMap {
        case Left(res) =>
          res.status match {
            case NOT_FOUND =>
              updateInstance(updateToBeStored, instanceRef, token, user.id).map {
                case Left(err) => Left(APIEditorMultiError(err.status, List(err)))
                case Right(()) => Right(())
              }
            case _ =>
              Future(Left(APIEditorMultiError.fromResponse(res.status, res.body)))
          }
        case Right(instance) =>
          val mergeInstanceWithPreviousUserUpdate = EditorInstance(
            InstanceHelper
              .removeInternalFields(instance)
              .merge(updateToBeStored.nexusInstance)
          )
          updateInstance(mergeInstanceWithPreviousUserUpdate, instanceRef, token, user.id).map {
            case Left(err) => Left(APIEditorMultiError(err.status, List(err)))
            case Right(()) => Right(())
          }
      }

  private def removeLinkFromInstance(
    refInstanceToUpdate: NexusInstanceReference,
    instanceIdToRemove: NexusInstanceReference,
    targetField: String,
    token: String,
    userID: String
  ) =
    retrieveInstance(refInstanceToUpdate, token).flatMap {
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
            updateInstance(EditorInstance(instance.copy(content = c)), refInstanceToUpdate, token, userID)
          case Right(None) => Future(Right(()))
          case Left(e) =>
            logger.error(e)
            Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, e)))
        }
    }

  def retrieveInstancesByIds(
    instanceIds: List[NexusInstanceReference],
    token: String
  ): Future[Either[APIEditorError, List[PreviewInstance]]] = {
    val listOfResponse = for {
      id <- instanceIds
    } yield
      queryService.getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        id,
        EditorService.kgQueryGetPreviewInstance(),
        token
      )

    Future.sequence(listOfResponse).map { ls =>
      val r = ls.filter(l => l.status == OK)
      if (r.isEmpty) {
        Left(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch all the instances"))
      } else {
        Right(r.map(res => res.json.as[PreviewInstance].setLabel(formService.formRegistry)))
      }
    }
  }

  def processReverseLinks(
    updateToBeStored: EditorInstance,
    updateReference: NexusInstanceReference,
    formRegistry: FormRegistry,
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
          logger.debug(s"Reverse entities remove link ${ref.toString}")
          removeLinkFromInstance(
            ref,
            updateReference,
            fieldName,
            token,
            user.id
          )
        } else {
          logger.debug(s"Reverse entities update ${ref.toString}")
          retrieveInstance(ref, token).flatMap[Either[APIEditorError, Unit]] {
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
              updateInstance(reverseLinkInstance, ref, token, user.id)
          }
        }
      }
    }
    (instanceWithoutReversLink, Future.sequence(instances.toList.flatten))
  }

}

object EditorService {

  def kgQueryGetPreviewInstance(
    context: String = EditorConstants.context
  ): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "fields": [
       |    {
       |      "fieldname": "name",
       |      "relative_path": "schema:name"
       |    },
       |    {
       |      "fieldname": "id",
       |      "relative_path": "base:relativeUrl"
       |    },
       |    {
       |      "fieldname": "description",
       |      "relative_path": "schema:description"
       |    },
       |    {
       |      "fieldname": "${UiConstants.DATATYPE}",
       |      "relative_path": "${JsonLDConstants.TYPE}"
       |    }
       |  ]
       |}
    """.stripMargin
}
