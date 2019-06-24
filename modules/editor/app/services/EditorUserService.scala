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

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.model.DateTime
import cats.syntax.either._
import cats.syntax.option._
import com.google.inject.Inject
import constants.EditorConstants
import models.AccessToken
import models.errors.APIEditorError
import models.instance.{NexusInstance, NexusInstanceReference}
import models.specification.QuerySpec
import models.user.{EditorUser, NexusUser}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import play.api.Logger
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient
import services.instance.InstanceApiService
import services.query.{QueryApiParameter, QueryService}

import scala.concurrent.duration._

object UserRequestMap {
  private val m = new ConcurrentHashMap[String, (DateTime, Task[Either[APIEditorError, EditorUser]])]

  def put(key: String, value: Task[Either[APIEditorError, EditorUser]]): Task[Either[APIEditorError, EditorUser]] = {
    this.m.put(key, (DateTime.now, value))._2
  }

  def get(key: String): Option[Task[Either[APIEditorError, EditorUser]]] = {
    val f = this.m.get(key)
    if (f != null) f._2.some else None
  }

  def cleanMap: Unit = {
    this.m.values().removeIf(p => p._1 <= DateTime.now.minus(600000L))
  }

}

class EditorUserService @Inject()(
  config: ConfigurationService,
  wSClient: WSClient,
  nexusService: NexusService,
  @NamedCache("editor-userinfo-cache") cache: AsyncCacheApi,
  nexusExtensionService: NexusExtensionService,
)(
  implicit oIDCAuthService: OIDCAuthService,
  credentials: CredentialsService,
  actorSystem: ActorSystem
) {
  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService
  object cacheService extends CacheService
  object queryService extends QueryService

  lazy val scheduler = scheduled

  def scheduled(implicit actorSystem: ActorSystem) =
    actorSystem.scheduler.schedule(initialDelay = 10.seconds, interval = 20.minute) {
      UserRequestMap.cleanMap
    }

  def getUser(nexusUser: NexusUser, token: AccessToken): Task[Either[APIEditorError, Option[EditorUser]]] = {
    cacheService.get[EditorUser](cache, nexusUser.id.toString).flatMap {
      case None =>
        queryService
          .getInstances(
            wSClient,
            config.kgQueryEndpoint,
            EditorConstants.editorUserPath,
            QuerySpec(Json.obj(), Some("kguser")),
            token,
            QueryApiParameter(vocab = Some(EditorConstants.editorVocab)),
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
      case Some(user) => Task.pure(user.some.asRight)
    }
  }

  private def cacheUser(userId: String, nexusUser: NexusUser): EditorUser = {
    val editorUser = EditorUser(NexusInstanceReference.fromUrl(userId), nexusUser)
    cache.set(editorUser.nexusUser.id, editorUser, config.cacheExpiration)
    editorUser
  }

  def getOrCreateUser(nexusUser: NexusUser, token: AccessToken)(
    afterCreation: (EditorUser, AccessToken) => Task[Either[APIEditorError, EditorUser]]
  ): Task[Either[APIEditorError, EditorUser]] =
    getUser(nexusUser, token).flatMap {
      case Right(Some(editorUser)) => Task.pure(editorUser.asRight)
      case Right(None) =>
        logger.debug("Calling second time get user returns None")
        UserRequestMap
          .get(nexusUser.id.toString) match {
          case Some(req) =>
            logger.debug(s"Fetching request from cache for user ${nexusUser.id.toString}")
            req
          case None =>
            logger.debug(s"Adding request to cache for user ${nexusUser.id.toString}")
            val task = Task.from {
              createUser(nexusUser, token).flatMap {
                case Right(editorUser) =>
                  logger.debug("Creating the user")
                  afterCreation(editorUser, token)
                case Left(e) => Task.pure(e.asLeft)
              }
            }.memoize
            UserRequestMap.put(
              nexusUser.id.toString,
              task
            )
            scheduler
            task
        }
      case Left(e) => Task.pure(e.asLeft)
    }

  def createUser(nexusUser: NexusUser, token: AccessToken): Task[Either[APIEditorError, EditorUser]] = {
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

  def deleteUser(editorUser: EditorUser, token: AccessToken): Task[Either[APIEditorError, Unit]] = {
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
