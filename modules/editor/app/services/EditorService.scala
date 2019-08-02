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
import constants._
import helpers._
import cats.implicits._
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance._
import models.specification.{FormRegistry, QuerySpec, UISpec}
import models.user.{IDMUser, User}
import models.{AccessToken, NexusPath}
import monix.eval.Task
import org.joda.time.DateTime
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.libs.ws.WSClient
import services.instance.InstanceApiService
import services.query.{QueryApiParameter, QueryService}
import services.specification.{FormOp, FormRegistries, FormService}

class EditorService @Inject()(
  wSClient: WSClient,
  config: ConfigurationService,
  formService: FormService
)(
  implicit OIDCAuthService: TokenAuthService,
  clientCredentials: CredentialsService
) {

  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService

  object queryService extends QueryService

  def retrievePreviewInstances(
    nexusPath: NexusPath,
    formRegistry: FormRegistry[UISpec],
    from: Option[Int],
    size: Option[Int],
    search: String,
    token: AccessToken
  ): Task[Either[APIEditorError, (List[PreviewInstance], Long)]] = {
    queryService
      .getInstances(
        wSClient,
        config.kgQueryEndpoint,
        nexusPath,
        QuerySpec(Json.obj(), Some("editorPreview")),
        token,
        QueryApiParameter(vocab = Some(EditorConstants.EDITORVOCAB), size = size, from = from, search = search)
      )
      .map { res =>
        res.status match {
          case OK =>
            Right(
              (
                (res.json \ "results")
                  .as[List[PreviewInstance]]
                  .map(_.setLabel(formRegistry)),
                (res.json \ "total").as[Long]
              )
            )
          case _ => Left(APIEditorError(res.status, res.body))
        }
      }
  }

  def insertInstance(
    newInstance: NexusInstance,
    user: Option[User],
    token: AccessToken
  ): Task[Either[APIEditorError, NexusInstanceReference]] = {
    val modifiedContent =
      FormOp.removeClientKeysCorrectLinks(newInstance.content.as[JsValue], config.nexusEndpoint)
    instanceApiService
      .post(wSClient, config.kgQueryEndpoint, newInstance.copy(content = modifiedContent), user.map(_.id), token)
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
    token: AccessToken,
    userId: String
  ): Task[Either[APIEditorError, Unit]] = {
    val contentWithUpdatedTimeStamp = diffInstance.nexusInstance.content.value
    val content =
      Json
        .toJson(
          contentWithUpdatedTimeStamp
            .updated(SchemaFieldsConstants.lastUpdate, Json.toJson(new DateTime().toDateTimeISO.toString))
        )
        .as[JsObject]
    instanceApiService
      .put(
        wSClient,
        config.kgQueryEndpoint,
        nexusInstanceReference,
        diffInstance.copy(nexusInstance = diffInstance.nexusInstance.copy(content = content)),
        token,
        userId
      )
      .map {
        case Left(res) => Left(APIEditorError(res.status, res.body))
        case Right(()) => Right(())
      }
  }

  def updateInstanceFromForm(
    instanceRef: NexusInstanceReference,
    form: Option[JsValue],
    user: IDMUser,
    token: AccessToken,
    reverseLinkService: ReverseLinkService
  ): Task[Either[APIEditorMultiError, Unit]] = {
    form match {
      case Some(json) =>
        formService.getRegistries().flatMap { registries =>
          registries.formRegistry.registry.get(instanceRef.nexusPath) match {
            case Some(spec)
                if spec.getFieldsAsMap.values.exists(p => p.isReverse.getOrElse(false)) |
                spec.getFieldsAsMap.values.exists(p => p.isLinkingInstance.getOrElse(false)) =>
              reverseLinkService
                .generateDiffAndUpdateInstanceWithReverseLink(
                  instanceRef,
                  json,
                  token,
                  user,
                  registries
                )
            case _ =>
              generateDiffAndUpdateInstance(
                instanceRef,
                json,
                token,
                user,
                registries
              )
          }
        }
      case None =>
        Task.pure(Left(APIEditorMultiError(BAD_REQUEST, List(APIEditorError(BAD_REQUEST, "No content provided")))))
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
    token: AccessToken,
    queryRegistry: FormRegistry[QuerySpec],
    databaseScope: Option[String] = None
  ): Task[Either[APIEditorError, NexusInstance]] = {
    queryRegistry.registry.get(nexusInstanceReference.nexusPath) match {
      case Some(querySpec) =>
        queryService
          .getInstancesWithId(
            wSClient,
            config.kgQueryEndpoint,
            nexusInstanceReference,
            querySpec,
            token,
            QueryApiParameter(vocab = Some(EditorConstants.EDITORVOCAB), databaseScope = databaseScope)
          )
          .map { res =>
            res.status match {
              case OK =>
                Right(
                  NexusInstance(
                    Some(nexusInstanceReference.id),
                    nexusInstanceReference.nexusPath,
                    res.json.as[JsObject]
                  )
                )
              case _ => Left(APIEditorError(res.status, res.body))
            }
          }
      case None =>
        instanceApiService
          .get(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
          .map {
            case Left(res)       => Left(APIEditorError(res.status, res.body))
            case Right(instance) => Right(instance)
          }
    }
  }

  def deleteLinkingInstance(
    from: NexusInstanceReference,
    to: NexusInstanceReference,
    linkingInstancePath: NexusPath,
    token: AccessToken
  ): Task[Either[APIEditorMultiError, Unit]] = {
    instanceApiService
      .getLinkingInstance(wSClient, config.kgQueryEndpoint, from, to, linkingInstancePath, token)
      .flatMap {
        case Right(ls) =>
          Task
            .gather(
              ls.map(
                l =>
                  instanceApiService
                    .delete(wSClient, config.kgQueryEndpoint, l, token)
                    .map[Either[APIEditorError, Unit]] {
                      case Left(res) => Left(APIEditorError(res.status, res.body))
                      case Right(_)  => Right(())
                  }
              )
            )
            .map { f =>
              val errors = f.filter(_.isLeft)
              if (errors.isEmpty) {
                Right(())
              } else {
                Left(APIEditorMultiError(INTERNAL_SERVER_ERROR, errors.map(_.swap.toOption.get)))
              }
            }
        case Left(res) => Task.pure(Left(APIEditorMultiError.fromResponse(res.status, res.body)))
      }
  }

  def generateDiffAndUpdateInstance(
    instanceRef: NexusInstanceReference,
    updateFromUser: JsValue,
    token: AccessToken,
    user: User,
    registries: FormRegistries
  ): Task[Either[APIEditorMultiError, Unit]] =
    retrieveInstance(instanceRef, token, registries.queryRegistry).flatMap {
      case Left(error) =>
        Task.pure(Left(APIEditorMultiError(error.status, List(error))))
      case Right(currentInstanceDisplayed) =>
        val updateToBeStored =
          EditorService.computeUpdateTobeStored(currentInstanceDisplayed, updateFromUser, config.nexusEndpoint)
        //Normal update of the instance without reverse links
        processInstanceUpdate(instanceRef, updateToBeStored, user, token)
    }

  /**
    * Update the reverse instance
    *
    * @param instanceRef      The reference to the instance being updated
    * @param updateToBeStored The instance being updated
    * @param user             The current user
    * @param token            The user token
    * @return
    */
  def processInstanceUpdate(
    instanceRef: NexusInstanceReference,
    updateToBeStored: EditorInstance,
    user: User,
    token: AccessToken
  ): Task[Either[APIEditorMultiError, Unit]] =
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
              Task.pure(Left(APIEditorMultiError.fromResponse(res.status, res.body)))
          }
        case Right(instance) =>
          val mergeInstanceWithPreviousUserUpdate = EditorInstance(
            InstanceOp
              .removeInternalFields(instance)
              .merge(updateToBeStored.nexusInstance)
          )
          updateInstance(mergeInstanceWithPreviousUserUpdate, instanceRef, token, user.id).map {
            case Left(err) => Left(APIEditorMultiError(err.status, List(err)))
            case Right(()) => Right(())
          }
      }

  def retrieveInstancesByIds(
    instanceIds: List[NexusInstanceReference],
    token: AccessToken,
    queryId: String,
    databaseScope: Option[String] = None
  ): Task[Either[APIEditorError, Seq[NexusInstance]]] = {
    instanceApiService
      .getByIdList(
        wSClient,
        config.kgQueryEndpoint,
        instanceIds,
        token,
        queryId,
        QueryApiParameter(vocab = Some(EditorConstants.EDITORVOCAB), databaseScope = databaseScope)
      )
      .map { res =>
        res.status match {
          case OK =>
            res.json
              .as[JsArray]
              .value
              .toList
              .map(EditorService.getIdForPayload)
              .sequence match {
              case Some(listOfIds) =>
                Right(
                  listOfIds.map {
                    case (instance, id) =>
                      val ref = NexusInstanceReference.fromUrl(id)
                      NexusInstance(Some(ref.id), ref.nexusPath, instance.as[JsObject])
                  }
                )
              case None =>
                Left(
                  APIEditorError(
                    INTERNAL_SERVER_ERROR,
                    s"Was not able to find an id definition of the instance. To make proper use of this endpoint, " +
                    s"you need either to define ${EditorService.atId}, ${EditorService.relativeURL}, " +
                    s"${EditorService.editorId} or ${EditorService.simpleId} in your query!"
                  )
                )
            }
          case _ => Left(APIEditorError(res.status, res.body))
        }
      }
  }

  def deleteInstance(
    nexusInstanceReference: NexusInstanceReference,
    token: AccessToken
  ): Task[Either[APIEditorError, Unit]] = {
    instanceApiService.deleteEditorInstance(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
  }

}

object EditorService {

  val atId = "@id"
  val relativeURL = s"${EditorConstants.BASENAMESPACE}${EditorConstants.RELATIVEURL}"
  val editorId = s"${EditorConstants.EDITORVOCAB}id"
  val simpleId = "id"

  def computeUpdateTobeStored(
    currentInstanceDisplayed: NexusInstance,
    updateFromUser: JsValue,
    nexusEndpoint: String
  ): EditorInstance = {
    val cleanedOriginalInstance =
      InstanceOp.removeInternalFields(currentInstanceDisplayed)
    val instanceUpdateFromUser =
      FormOp.buildInstanceFromForm(cleanedOriginalInstance, updateFromUser, nexusEndpoint)
    val currentInstanceWithSameFormatAsNewValue =
      FormOp.removeClientKeysCorrectLinks(cleanedOriginalInstance.content, nexusEndpoint)
    val diff = InstanceOp.buildDiffEntity(
      cleanedOriginalInstance.copy(content = currentInstanceWithSameFormatAsNewValue),
      instanceUpdateFromUser
    )
    InstanceOp.removeEmptyFieldsNotInOriginal(cleanedOriginalInstance, diff)
  }

  def getIdForPayload(instance: JsValue): Option[(JsValue, String)] = {
    if ((instance \ EditorService.atId).isDefined) {
      Some(instance, (instance \ EditorService.atId).get.as[String])
    } else if ((instance \ EditorService.relativeURL).isDefined) {
      Some(instance, (instance \ EditorService.relativeURL).get.as[String])
    } else if ((instance \ EditorService.editorId).isDefined) {
      Some(instance, (instance \ EditorService.editorId).get.as[String])
    } else if ((instance \ EditorService.simpleId).isDefined) {
      Some(instance, (instance \ EditorService.simpleId).get.as[String])
    } else {
      None
    }
  }

}
