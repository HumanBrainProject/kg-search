/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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
package services.review

import com.google.inject.Inject
import constants.{SchemaFieldsConstants, SuggestionStatus}
import models.AccessToken
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{NexusInstanceReference, SuggestionInstance}
import models.user.{EditorUser, NexusUser}
import monix.eval.Task
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import services.instance.InstanceApiService
import services.review.ReviewService.UserID
import services._

import scala.concurrent.{ExecutionContext, Future}

class ReviewService @Inject()(
  wSClient: WSClient,
  config: ConfigurationService,
  editorService: EditorService,
  reverseLinkService: ReverseLinkService
)(
  implicit executionContext: ExecutionContext,
  OIDCAuthService: OIDCAuthService,
  clientCredentials: CredentialsService
) {
  object ReviewApiService extends ReviewApiService
  object InstanceApiService extends InstanceApiService

  def addUsersToInstance(
    instance: NexusInstanceReference,
    users: List[UserID],
    token: AccessToken,
    user: EditorUser
  ): Task[Either[APIEditorMultiError, Unit]] = {
    val f: List[Task[Either[APIEditorError, Unit]]] = for {
      id <- users
    } yield ReviewApiService.addUserToSuggestionInstance(wSClient, config.kgQueryEndpoint, instance, id, token, user)

    Task.gather(f).map { l =>
      val err = l.collect {
        case Left(e) => e
      }
      if (err.isEmpty) {
        Right(())
      } else {
        Left(APIEditorMultiError(INTERNAL_SERVER_ERROR, err))
      }
    }
  }

  def acceptSuggestion(
    suggestionRef: NexusInstanceReference,
    update: Option[JsValue],
    nexusUser: NexusUser,
    token: AccessToken
  ): Task[Either[APIEditorMultiError, Unit]] = {
    InstanceApiService.get(wSClient, config.kgQueryEndpoint, suggestionRef, token).flatMap {
      case Right(suggestionInstance) =>
        val instanceUpdateRef = NexusInstanceReference.fromUrl(
          (suggestionInstance.content \ SchemaFieldsConstants.SUGGESTION_OF \ SchemaFieldsConstants.RELATIVEURL)
            .as[String]
        )
        editorService
          .updateInstanceFromForm(
            instanceUpdateRef,
            update,
            nexusUser,
            token,
            reverseLinkService
          )
          .flatMap {
            case Right(()) =>
              ReviewApiService.acceptSuggestion(wSClient, config.kgQueryEndpoint, suggestionRef, token).map {
                case Right(()) => Right(())
                case Left(err) => Left(APIEditorMultiError(err.status, List(err)))
              }
            case Left(err) => Task.pure(Left(err))
          }
      case Left(res) => Task.pure(Left(APIEditorMultiError(res.status, List(APIEditorError(res.status, res.body)))))
    }

  }

  def rejectSuggestion(
    nexusInstanceReference: NexusInstanceReference,
    token: AccessToken
  ): Task[Either[APIEditorError, Unit]] = {
    ReviewApiService.rejectSuggestion(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
  }

  def getUsersSuggestions(
    suggestionStatus: SuggestionStatus,
    token: AccessToken
  ): Task[Either[APIEditorError, List[SuggestionInstance]]] = {
    ReviewApiService.getUsersSuggestions(wSClient, config.kgQueryEndpoint, suggestionStatus, token)
  }

  def getInstanceSuggestions(
    ref: NexusInstanceReference,
    token: AccessToken
  ): Task[Either[APIEditorError, List[SuggestionInstance]]] = {
    ReviewApiService.getInstanceSuggestions(wSClient, config.kgQueryEndpoint, ref, token)
  }
}

object ReviewService {
  type UserID = String
}
