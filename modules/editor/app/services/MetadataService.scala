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
import models.errors.APIEditorError
import models.instance.{EditorMetadata, NexusInstance, NexusInstanceReference}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MetadataService @Inject()(IDMAPIService: IDMAPIService, authService: OIDCAuthService, WSClient: WSClient)(
  implicit executionContext: ExecutionContext
) {

  def getMetadata(
    nexusInstanceReference: NexusInstanceReference,
    instance: NexusInstance
  ): Future[Either[APIEditorError, EditorMetadata]] = {
    val (lastUpdaterId, lastUpdate) = MetadataService.extractMetadataFromInstance(instance)
    val updater = for {
      token   <- authService.getTechAccessToken()
      updater <- IDMAPIService.getUserInfoFromID(lastUpdaterId, token)
    } yield updater

    updater.map {
      case Some(user) =>
        Right(
          EditorMetadata(
            lastUpdate,
            None,
            user.displayName,
            25
          )
        )
      case None =>
        Right(
          EditorMetadata(
            lastUpdate,
            None,
            "Unknown user",
            25
          )
        )
    }

  }
}

object MetadataService {

  def extractMetadataFromInstance(instance: NexusInstance): (String, Option[DateTime]) = {
    val lastUpdater = (instance.content \ "https://schema.hbp.eu/provenance/lastModificationUserId").as[String] match {
      case s if s.startsWith("https://nexus-iam-dev.humanbrainproject.org") =>
        s.splitAt(s.lastIndexOf("/"))._2.substring(1)
      case s => s
    }
    val lastUpdate =
      Try(
        DateTimeFormat
          .forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
          .parseDateTime((instance.content \ "https://schema.hbp.eu/provenance/modifiedAt").as[String])
      ).toOption
    (lastUpdater, lastUpdate)
  }
}
