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
package services.suggestion

import com.google.inject.Inject
import constants.{SchemaFieldsConstants, SuggestionStatus}
import models.errors.{APIEditorError, APIEditorMultiError}
import models.instance.{NexusInstanceReference, SuggestionInstance}
import models.user.{EditorUser, NexusUser}
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import services.{ConfigurationService, EditorService, ReverseLinkService}
import services.suggestion.SuggestionService.UserID
import services.instance.InstanceApiService

import scala.concurrent.{ExecutionContext, Future}

class SuggestionService @Inject()(
  wSClient: WSClient,
  config: ConfigurationService,
  editorService: EditorService,
  reverseLinkService: ReverseLinkService
)(
  implicit executionContext: ExecutionContext
) {
  object SuggestionApiService extends SuggestionApiService
  object InstanceApiService extends InstanceApiService

  def addUsersToInstance(
    instance: NexusInstanceReference,
    users: List[UserID],
    token: String,
    user: EditorUser
  ): Future[Either[APIEditorMultiError, Unit]] = {
    val f: List[Future[Either[APIEditorError, Unit]]] = for {
      id <- users
    } yield
      SuggestionApiService.addUserToSuggestionInstance(wSClient, config.kgQueryEndpoint, instance, id, token, user)

    Future.sequence(f).map { l =>
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
    token: String
  ): Future[Either[APIEditorMultiError, Unit]] = {
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
              SuggestionApiService.acceptSuggestion(wSClient, config.kgQueryEndpoint, suggestionRef, token).map {
                case Right(()) => Right(())
                case Left(err) => Left(APIEditorMultiError(err.status, List(err)))
              }
            case Left(err) => Future(Left(err))
          }
      case Left(res) => Future(Left(APIEditorMultiError(res.status, List(APIEditorError(res.status, res.body)))))
    }

  }

  def rejectSuggestion(
    nexusInstanceReference: NexusInstanceReference,
    token: String
  ): Future[Either[APIEditorError, Unit]] = {
    SuggestionApiService.rejectSuggestion(wSClient, config.kgQueryEndpoint, nexusInstanceReference, token)
  }

  def getUsersSuggestions(
    suggestionStatus: SuggestionStatus,
    token: String
  ): Future[Either[APIEditorError, List[SuggestionInstance]]] = {
    SuggestionApiService.getUsersSuggestions(wSClient, config.kgQueryEndpoint, suggestionStatus, token)
  }

  def getInstanceSuggestions(
    ref: NexusInstanceReference,
    token: String
  ): Future[Either[APIEditorError, List[SuggestionInstance]]] = {
    SuggestionApiService.getInstanceSuggestions(wSClient, config.kgQueryEndpoint, ref, token)
  }
}

object SuggestionService {
  type UserID = String
}
