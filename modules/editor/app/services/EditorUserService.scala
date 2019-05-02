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
import constants.EditorConstants
import models.errors.APIEditorError
import models.instance.{NexusInstance, NexusInstanceReference}
import models.user.{EditorUser, NexusUser}
import play.api.Logger
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient
import services.instance.InstanceApiService
import services.query.QueryService

import scala.concurrent.{ExecutionContext, Future}
import cats.syntax.either._
import cats.syntax.option._

class EditorUserService @Inject()(
  config: ConfigurationService,
  wSClient: WSClient,
  nexusService: NexusService,
  @NamedCache("editor-userinfo-cache") cache: AsyncCacheApi,
  nexusExtensionService: NexusExtensionService,
  oIDCAuthService: OIDCAuthService,
)(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService
  object cacheService extends CacheService
  object queryService extends QueryService

  def getUser(nexusUser: NexusUser, token: String): Future[Either[APIEditorError, Option[EditorUser]]] = {
    cacheService.get[EditorUser](cache, nexusUser.id.toString).flatMap {
      case None =>
        queryService
          .getInstancesWithStoredQuery(
            wSClient,
            config.kgQueryEndpoint,
            EditorConstants.editorUserPath,
            "kguser",
            token,
            None,
            None,
            "",
            vocab = Some(EditorConstants.editorVocab),
            Map("userId" -> nexusUser.id)
          )
          .map { res =>
            res.status match {
              case OK =>
                val users = (res.json \ "results")
                  .as[List[JsObject]]
                if (users.size > 1) {
                  val msg = s"Multiple user with the same ID detected: ${users.map(js => js \ "nexusId").mkString(" ")}"
                  logger.error(msg)
                  val id = (users.head \ "nexusId").as[String]
                  cacheUser(id, nexusUser).some.asRight
                } else if (users.size == 1) {
                  val id = (users.head \ "nexusId").as[String]
                  cacheUser(id, nexusUser).some.asRight
                } else {
                  None.asRight
                }
              case _ =>
                logger.error(s"Could not fetch the user with ID ${nexusUser.id} ${res.body}")
                APIEditorError(res.status, res.body).asLeft
            }
          }
      case Some(user) => Future.successful(user.some.asRight)
    }
  }

  private def cacheUser(userId: String, nexusUser: NexusUser): EditorUser = {
    val editorUser = EditorUser(NexusInstanceReference.fromUrl(userId), nexusUser)
    cache.set(editorUser.nexusUser.id, editorUser, config.cacheExpiration)
    editorUser
  }

  def getOrCreateUser(nexusUser: NexusUser, token: String)(
    afterCreation: (EditorUser, String) => Future[Either[APIEditorError, EditorUser]]
  ): Future[Either[APIEditorError, EditorUser]] =
    getUser(nexusUser, token).flatMap[Either[APIEditorError, EditorUser]] {
      case Right(Some(editorUser)) => Future(editorUser.asRight)
      case Right(None) =>
        val r = for {
          refreshedToken <- oIDCAuthService.getTechAccessToken(true)
          res            <- getUser(nexusUser, refreshedToken)
        } yield res
        r.flatMap {
          case Right(Some(editorUser)) => Future.successful(editorUser.asRight)
          case Right(None) =>
            createUser(nexusUser, token).flatMap {
              case Right(editorUser) => afterCreation(editorUser, token)
              case Left(e)           => Future.successful(e.asLeft)
            }
          case Left(e) => Future.successful(e.asLeft)
        }
      case Left(err) => Future.successful(err.asLeft)
    }

  def createUser(nexusUser: NexusUser, token: String): Future[Either[APIEditorError, EditorUser]] = {
    instanceApiService
      .post(
        wSClient,
        config.kgQueryEndpoint,
        NexusInstance(None, EditorConstants.editorUserPath, EditorUserService.userToNexusStruct(nexusUser.id)),
        None,
        token
      )
      .map {
        case Right(ref) => EditorUser(ref, nexusUser).asRight
        case Left(res) =>
          logger.error(s"Could not create the user with ID ${nexusUser.id} - ${res.body}")
          APIEditorError(res.status, res.body).asLeft
      }
  }

  def deleteUser(editorUser: EditorUser, token: String): Future[Either[APIEditorError, Unit]] = {
    instanceApiService
      .delete(wSClient, config.kgQueryEndpoint, editorUser.nexusId, token)
      .map {
        case Right(_) => ().asRight
        case Left(res) =>
          logger.error(s"Could not delete the user with ID ${editorUser.nexusId.toString} - ${res.body}")
          APIEditorError(res.status, res.body).asLeft
      }
  }
}

object EditorUserService {

  def userToNexusStruct(userId: String): JsObject = {
    Json.obj(
      EditorConstants.EDITORNAMESPACE + EditorConstants.USERID -> userId
    )
  }

}
