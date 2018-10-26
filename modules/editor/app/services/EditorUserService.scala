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

package editor.services

import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.{Favorite, FavoriteGroup, NexusInstance, NexusPath}
import common.services.ConfigurationService
import editor.helpers.InstanceHelper
import editor.models.EditorUser
import editor.models.EditorUserList.FolderType
import helpers.ResponseHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.http.ContentTypes._

import scala.concurrent.{ExecutionContext, Future}

class EditorUserService @Inject()(config: ConfigurationService,
                                  wSClient: WSClient,
                                  nexusService: NexusService,
                                  oIDCAuthService: OIDCAuthService,
                                 )(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  def getUser(id: String): Future[Option[EditorUser]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorUserService.kgQueryGetUserQuery).map{
      res => res.status match {
        case OK => (res.json \ "results").as[List[EditorUser]].find(user => user.id == id)
        case _ =>
          logger.error(s"Could not fetch the user with ID $id " + res.body)
          None
      }
    }
  }


  def createUser(userId: String, token: String): Future[Option[EditorUser]] = {
    nexusService.createSimpleSchema(
      config.nexusEndpoint,
      EditorUserService.editorUserPath,
      token,
      Some(EditorUserService.editorNameSpace)
    ).flatMap{ res =>
      res.status match {
        case OK | CREATED | CONFLICT =>
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorUserService.editorUserPath,
            EditorUserService.userToNexusStruct(userId),
            token
          ).map { res =>
            res.status match {
              case OK | CREATED =>
                val (id, path) = NexusInstance.extractIdAndPath(res.json)
                Some(EditorUser(s"${path.toString}/$id", userId, List()))
              case _ => None
            }
          }
        case _ =>
          logger.error("Could not create editor User schema")
          Future(None)
      }
    }

  }
//
//  def addFavorite(favoriteGroupId: String, instance: String): Future[Option[Favorite]] = {
//    oIDCAuthService.getTechAccessToken().flatMap {
//      token =>
//
//        val payload = EditorUserService.favoriteToNexusStruct(
//          s"${config.nexusEndpoint}/v0/data/$instance",
//          s"${config.nexusEndpoint}/v0/data/${favoriteGroupId}")
//        nexusService.insertInstance(
//          config.nexusEndpoint,
//          EditorUserService.favoritePath,
//          payload,
//          token
//        ).map{
//          res => res.status match{
//            case CREATED => Some(Favorite((res.json \ "@id").as[String].split("v0/data/").last, instance))
//            case _ =>
//              logger.error("Error while creating a favorite " + res.body)
//              None
//          }
//        }
//
//    }
//  }
//
//  def removeFavorite(favoriteId : String): Future[WSResponse] = {
//    oIDCAuthService.getTechAccessToken().flatMap {
//      token =>
//        nexusService.deprecateInstance(
//          config.nexusEndpoint,
//          EditorUserService.favoritePath,
//          favoriteId,
//          token
//        )
//    }
//  }

  def getOrCreate(userId: String, token: String ): Future[Option[EditorUser]] = {
    this.getUser(userId).flatMap{
      case None => this.createUser(userId, token)
      case Some(u) => Future{Some(u)}
    }
  }


}

object EditorUserService {
  val editorUserPath = NexusPath("kgeditor", "core", "user", "v0.0.1")
  val userFolderPath = NexusPath("kgeditor", "core", "userfolder", "v0.0.1")
  val userBookmarkListPath = NexusPath("kgeditor", "core", "userbookmarklist", "v0.0.1")
  val userBookmarkPath = NexusPath("kgeditor", "core", "userbookmark", "v0.0.1")
  val editorNameSpace = "http://hbp.eu/kgeditor/"

  val kgQueryGetUserQuery =
    s"""
       |{
       |  "@context": {
       |    "@vocab": "http://schema.hbp.eu/graph_query/",
       |    "schema": "http://schema.org/",
       |    "kgeditor": "http://hbp.eu/kgeditor/",
       |    "nexus": "https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/",
       |    "nexus_instance": "https://nexus-dev.humanbrainproject.org/v0/schemas/",
       |    "this": "http://schema.hbp.eu/instances/",
       |    "searchui": "http://schema.hbp.eu/search_ui/",
       |    "fieldname": {
       |      "@id": "fieldname",
       |      "@type": "@id"
       |    },
       |    "merge": {
       |      "@id": "merge",
       |      "@type": "@id"
       |    },
       |    "relative_path": {
       |      "@id": "relative_path",
       |      "@type": "@id"
       |    },
       |    "root_schema": {
       |      "@id": "root_schema",
       |      "@type": "@id"
       |    }
       |  },
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${editorUserPath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname": "nexusId",
       |      "required": true,
       |      "relative_path": "@id"
       |    },
       |    {
       |      "fieldname": "userId",
       |      "required": true,
       |      "relative_path": "schema:identifier"
       |    }
       |  ]
       |}
    """.stripMargin




  def userToNexusStruct(userId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> userId,
      "@type" -> s"${editorNameSpace}User"
    )
  }

  def bookmarkToNexusStruct(bookmark: String, userBookMarkListNexusId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userBookMarkListNexusId + bookmark),
      "http://hbp.eu/kgeditor/userBookMarkList" -> Json.obj("@id" -> s"$userBookMarkListNexusId"),
      "http://hbp.eu/kgeditor/bookmarkInstanceLink" -> Json.obj("@id" -> s"$bookmark"),
      "@type" -> s"${editorNameSpace}Userbookmark"
    )
  }

  def bookmarkListToNexusStruct(name:String, userFolderId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userFolderId + name),
      "http://schema.org/name" -> name,
      "http://hbp.eu/kgeditor/userFolder" -> Json.obj("@id" -> s"$userFolderId"),
      "@type" -> s"${editorNameSpace}Userbookmarklist"
    )
  }

  def userFolderToNexusStruct(name:String, userNexusId: String, folderType: FolderType) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userNexusId + name),
      "http://schema.org/name" -> name,
      "http://hbp.eu/kgeditor/user" -> Json.obj("@id" -> s"$userNexusId"),
      "http://hbp.eu/kgeditor/folderType" -> folderType.t,
      "@type" -> s"${editorNameSpace}Userfolder"
    )
  }
}
