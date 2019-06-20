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
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance._
import models.specification.{FormRegistry, QuerySpec, UISpec}
import models.user.{NexusUser, User}
import models.{AccessToken, NexusPath}
import org.joda.time.DateTime
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSClient
import services.instance.InstanceApiService
import services.query.{QueryApiParameter, QueryService}
import services.specification.{FormRegistries, FormService}

import scala.concurrent.{ExecutionContext, Future}

class EditorService @Inject()(
  wSClient: WSClient,
  config: ConfigurationService,
  formService: FormService
)(
  implicit executionContext: ExecutionContext,
  OIDCAuthService: OIDCAuthService,
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
  ): Future[Either[APIEditorError, (List[PreviewInstance], Long)]] = {
    val promotedFields = (for {
      spec   <- formRegistry.registry.get(nexusPath)
      uiInfo <- spec.uiInfo
    } yield {
      (uiInfo.promotedFields.headOption, uiInfo.promotedFields.tail.headOption)
    }).getOrElse((None, None))

    val query = EditorService.kgQueryGetPreviewInstance(promotedFields._1, promotedFields._2)
    queryService
      .getInstances(
        wSClient,
        config.kgQueryEndpoint,
        nexusPath,
        QuerySpec(Json.parse(query).as[JsObject]),
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

  def retrievePreviewInstancesByIds(
    instanceIds: List[NexusInstanceReference],
    registries: FormRegistries,
    token: AccessToken,
  ): Future[List[PreviewInstance]] = {
    val listOfResponse = for {
      id <- instanceIds
    } yield {
      val promotedFields = (for {
        spec   <- registries.formRegistry.registry.get(id.nexusPath)
        uiInfo <- spec.uiInfo
      } yield {
        (uiInfo.promotedFields.headOption, uiInfo.promotedFields.tail.headOption)
      }).getOrElse((None, None))
      val query = EditorService.kgQueryGetPreviewInstance(promotedFields._1, promotedFields._2)
      queryService
        .getInstancesWithId(
          wSClient,
          config.kgQueryEndpoint,
          id,
          QuerySpec(Json.parse(query).as[JsObject]),
          token,
          QueryApiParameter(vocab = Some(EditorConstants.EDITORVOCAB))
        )
        .map { res =>
          res.status match {
            case OK => Right(res.json.as[PreviewInstance].setLabel(registries.formRegistry))
            case _  => Left(APIEditorError(res.status, "Could not fetch all the instances"))
          }
        }
    }

    Future.sequence(listOfResponse).map { ls =>
      val r = ls.filter(_.isRight)
      r.collect { case Right(e) => e }
    }
  }

  def insertInstance(
    newInstance: NexusInstance,
    user: Option[User],
    token: AccessToken
  ): Future[Either[APIEditorError, NexusInstanceReference]] = {
    val modifiedContent =
      FormService.removeClientKeysCorrectLinks(newInstance.content.as[JsValue], config.nexusEndpoint)
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
  ): Future[Either[APIEditorError, Unit]] = {
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
    user: NexusUser,
    token: AccessToken,
    reverseLinkService: ReverseLinkService
  ): Future[Either[APIEditorMultiError, Unit]] = {
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
        Future(Left(APIEditorMultiError(BAD_REQUEST, List(APIEditorError(BAD_REQUEST, "No content provided")))))
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
  ): Future[Either[APIEditorError, NexusInstance]] = {
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
  ): Future[Either[APIEditorMultiError, Unit]] = {
    instanceApiService
      .getLinkingInstance(wSClient, config.kgQueryEndpoint, from, to, linkingInstancePath, token)
      .flatMap {
        case Right(ls) =>
          Future
            .sequence(
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
        case Left(res) => Future(Left(APIEditorMultiError.fromResponse(res.status, res.body)))
      }
  }

  def generateDiffAndUpdateInstance(
    instanceRef: NexusInstanceReference,
    updateFromUser: JsValue,
    token: AccessToken,
    user: User,
    registries: FormRegistries
  ): Future[Either[APIEditorMultiError, Unit]] =
    retrieveInstance(instanceRef, token, registries.queryRegistry).flatMap {
      case Left(error) =>
        Future(Left(APIEditorMultiError(error.status, List(error))))
      case Right(currentInstanceDisplayed) =>
        val updateToBeStored =
          EditorService.computeUpdateTobeStored(currentInstanceDisplayed, updateFromUser, config.nexusEndpoint)
        //Normal update of the instance without reverse links
        processInstanceUpdate(instanceRef, updateToBeStored, user, token)
    }

  /**
    *  Update the reverse instance
    * @param instanceRef The reference to the instance being updated
    * @param updateToBeStored The instance being updated
    * @param user The current user
    * @param token The user token
    * @return
    */
  def processInstanceUpdate(
    instanceRef: NexusInstanceReference,
    updateToBeStored: EditorInstance,
    user: User,
    token: AccessToken
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

  def retrieveInstancesByIds(
    instanceIds: List[NexusInstanceReference],
    querySpec: FormRegistry[QuerySpec],
    token: AccessToken,
  ): Future[Either[APIEditorError, List[NexusInstance]]] = {
    val listOfResponse = for {
      id <- instanceIds
    } yield retrieveInstance(id, token, querySpec)
    Future.sequence(listOfResponse).map { ls =>
      val r = ls.filter(_.isRight)
      if (r.isEmpty) {
        val error = ls.head.swap.getOrElse(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch all the instances"))
        Left(error)
      } else {
        Right(r.collect { case Right(e) => e })
      }

    }
  }

  def deleteInstance(
    nexusInstanceReference: NexusInstanceReference,
    token: AccessToken
  ): Future[Either[APIEditorError, Unit]] = {
    instanceApiService.deleteEditorInstance(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
  }

}

object EditorService {

  def computeUpdateTobeStored(
    currentInstanceDisplayed: NexusInstance,
    updateFromUser: JsValue,
    nexusEndpoint: String
  ): EditorInstance = {
    val cleanedOriginalInstance =
      InstanceHelper.removeInternalFields(currentInstanceDisplayed)
    val instanceUpdateFromUser =
      FormService.buildInstanceFromForm(cleanedOriginalInstance, updateFromUser, nexusEndpoint)
    val currentInstanceWithSameFormatAsNewValue =
      FormService.removeClientKeysCorrectLinks(cleanedOriginalInstance.content, nexusEndpoint)
    val diff = InstanceHelper.buildDiffEntity(
      cleanedOriginalInstance.copy(content = currentInstanceWithSameFormatAsNewValue),
      instanceUpdateFromUser
    )
    InstanceHelper.removeEmptyFieldsNotInOriginal(cleanedOriginalInstance, diff)
  }

  def kgQueryGetPreviewInstance(
    nameField: Option[String] = None,
    descriptionField: Option[String] = None,
    context: String = EditorConstants.context
  ): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "fields": [
       |    {
       |      "fieldname": "this:name",
       |      "relative_path": "${nameField.getOrElse("schema:name")}",
       |      "sort":true
       |    },
       |    {
       |      "fieldname": "this:id",
       |      "relative_path": "base:relativeUrl"
       |    },
       |    {
       |      "fieldname": "this:description",
       |      "relative_path": "${descriptionField.getOrElse("schema:description")}"
       |    },
       |    {
       |      "fieldname": "this:${UiConstants.DATATYPE}",
       |      "relative_path": "${JsonLDConstants.TYPE}",
       |      "required":true
       |    }
       |  ]
       |}
    """.stripMargin
}
