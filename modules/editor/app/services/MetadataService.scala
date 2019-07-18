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
package services

import com.google.inject.Inject
import constants.{InternalSchemaFieldsConstants, SchemaFieldsConstants}
import models.errors.APIEditorError
import models.instance.{EditorMetadata, NexusInstance}
import models.user.IDMUser
import monix.eval.Task
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import play.api.cache.{AsyncCacheApi, NamedCache}
import scala.concurrent.duration._
import play.api.libs.ws.WSClient

import scala.util.Try

class MetadataService @Inject()(
  IDMAPIService: IDMAPIService,
  authService: TokenAuthService,
  WSClient: WSClient,
  @NamedCache("editor-metadata-cache") cache: AsyncCacheApi
) {

  def getMetadata(
    instance: NexusInstance
  ): Task[Either[APIEditorError, EditorMetadata]] = {
    val (lastUpdaterIdOpt, createdByIdOpt, partialMetadata) = MetadataService.extractMetadataFromInstance(instance)
    for {
      updater   <- MetadataService.getUserFromMetadata(lastUpdaterIdOpt, authService, IDMAPIService, cache)
      createdBy <- MetadataService.getUserFromMetadata(createdByIdOpt, authService, IDMAPIService, cache)
    } yield {
      Right(
        partialMetadata.copy(
          lastUpdateBy = updater.map(_.displayName).getOrElse(MetadataService.UNKNOWN_USER),
          createdBy = createdBy.map(_.displayName).getOrElse(MetadataService.UNKNOWN_USER)
        )
      )
    }
  }
}

object MetadataService {
  object cacheService extends CacheService
  implicit val scheduler = monix.execution.Scheduler.Implicits.global
  val logger = LoggerFactory.getLogger(this.getClass)
  val UNKNOWN_USER = "Unknown user"
  private def parseJsFieldAsDate(instance: NexusInstance, field: String): Try[DateTime] = {
    Try(
      DateTimeFormat
        .forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
        .parseDateTime((instance.content \ field).as[String])
    )
  }

  def getUserFromMetadata(
    userIdOpt: Option[String],
    authService: TokenAuthService,
    IDMAPIService: IDMAPIService,
    cacheApi: AsyncCacheApi
  ): Task[Option[IDMUser]] = {
    userIdOpt match {
      case Some(userIdO) =>
        val id = if (userIdO.startsWith("https://")) {
          userIdO.split("/").last
        } else {
          userIdO
        }
        logger.debug(s"Fetching metadata for user ${id}")
        cacheService
          .getOrElse[IDMUser](cacheApi, id) {
            val u = for {
              token <- authService.getTechAccessToken()
              user  <- IDMAPIService.getUserInfoFromID(id, token)
            } yield user
            u.flatMap {
              case Some(idmUser) =>
                cacheService.set[IDMUser](cacheApi, id, idmUser, 2.hours).map { _ =>
                  Some(idmUser)
                }
              case None => Task.pure(None)
            }
          }
      case None => Task.pure(None)
    }
  }

  private def extractUserId(instance: NexusInstance, field: String): Option[String] = {
    (instance.content \ field).asOpt[String] match {
      case Some(s) if s.startsWith("https://nexus-iam-dev.humanbrainproject.org") =>
        Some(s.splitAt(s.lastIndexOf("/"))._2.substring(1))
      case Some(s) => Some(s)
      case None    => None
    }
  }

  def extractMetadataFromInstance(instance: NexusInstance): (Option[String], Option[String], EditorMetadata) = {
    val lastUpdater = extractUserId(instance, SchemaFieldsConstants.lastUpater)
    val lastUpdate =
      parseJsFieldAsDate(instance, SchemaFieldsConstants.lastUpdate).toOption
    val numberOfUpdates = (instance.content \ InternalSchemaFieldsConstants.NEXUS_REV).asOpt[Long].getOrElse(-1L)
    val creationDate =
      parseJsFieldAsDate(instance, SchemaFieldsConstants.createdAt).toOption
    val createdBy = extractUserId(instance, SchemaFieldsConstants.createdBy)
    (lastUpdater, createdBy, EditorMetadata(UNKNOWN_USER, creationDate, UNKNOWN_USER, lastUpdate, numberOfUpdates))
  }
}
