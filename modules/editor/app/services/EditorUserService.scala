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
import common.models.{EditorUser, Favorite, FavoriteGroup, NexusPath}
import common.services.ConfigurationService
import editor.helper.InstanceHelper
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
                                  oIDCAuthService: OIDCAuthService
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

  def createUser(userId: String): Future[Option[EditorUser]] = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>
        // Create the user in the nexus
        nexusService.insertInstance(
          config.nexusEndpoint,
          EditorUserService.editorUserPath,
          EditorUserService.userToNexusStruct(userId),
          token
        ).map{
          res =>
            res.status match {
              case OK | CREATED =>
                val nexusId = (res.json \ "@id").as[String]
                Some(EditorUser(nexusId, userId, Seq()))
              case _ => None
            }
        }
    }
  }

  def addFavorite(favoriteGroupId: String, instance: String): Future[Option[Favorite]] = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>

        val payload = EditorUserService.favoriteToNexusStruct(
          s"${config.nexusEndpoint}/v0/data/$instance",
          s"${config.nexusEndpoint}/v0/data/${favoriteGroupId}")
        nexusService.insertInstance(
          config.nexusEndpoint,
          EditorUserService.favoritePath,
          payload,
          token
        ).map{
          res => res.status match{
            case CREATED => Some(Favorite((res.json \ "@id").as[String].split("v0/data/").last, instance))
            case _ =>
              logger.error("Error while creating a favorite " + res.body)
              None
          }
        }

    }
  }

  def removeFavorite(favoriteId : String): Future[WSResponse] = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>
        nexusService.deprecateInstance(
          config.nexusEndpoint,
          EditorUserService.favoritePath,
          favoriteId,
          token
        )
    }
  }

  def createFavoriteGroup(name: String, userId:String): Future[Option[FavoriteGroup]]  = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>
        getUser(userId).flatMap{
          case Some(user) =>
            val payload = EditorUserService.favoriteGroupToNexusStruct(name, s"${config.nexusEndpoint}/v0/data/${user.nexusId}" )
            nexusService.insertInstance(
              config.nexusEndpoint,
              EditorUserService.favoriteGroupPath,
              payload,
              token
            ).map{
              res => res.status match{
                case CREATED => Some(FavoriteGroup((res.json \ "@id").as[String].split("v0/data/").last, name, Seq()))
                case _ =>
                  logger.error("Error while creating a favorite " + res.body)
                  None
              }
            }
          case None =>
            logger.error("Error while creating a favorite. User not found")
            Future{None}
        }
    }

  }

  def getOrCreate(userId: String ): Future[Option[EditorUser]] = {
    this.getUser(userId).flatMap{
      case None => this.createUser(userId)
      case Some(u) => Future{Some(u)}
    }
  }


}

object EditorUserService {
  val editorUserPath = NexusPath("kgeditor", "core", "user", "v0.0.2")
  val favoriteGroupPath = NexusPath("kgeditor", "core", "favoriteGroup", "v0.0.1")
  val favoritePath = NexusPath("kgeditor", "core", "favorite", "v0.0.2")

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
       |    },
       |    {
       |        "fieldname": "favoriteGroups",
       |        "relative_path": {
       |                "@id": "kgeditor:user",
       |                "reverse":true
       |        },
       |        "fields": [
       |           {
       |             "fieldname": "name",
       |             "relative_path": "schema:name",
       |             "required": true
       |           },
       |           {
       |             "fieldname": "nexusId",
       |             "relative_path": "@id",
       |             "required": true
       |           },
       |           {
       |             "fieldname": "favorites",
       |             "relative_path": {
       |                 "@id": "kgeditor:favoriteGroup",
       |                 "reverse":true
       |               },
       |               "fields":[
       |               		{
       |               			"fieldname":"nexusId",
       |               			"relative_path": "@id"
       |               		},
       |               		{
       |               			"fieldname":"instance",
       |               			"relative_path": [
       |               				"kgeditor:favoriteInstance"
       |               			]
       |               		}
       |
       |               	]
       |
       |           }
       |        ]
       |    }
       |  ]
       |}
    """.stripMargin


  def userToNexusStruct(userId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> userId,
      "@type" -> "http://hbp.eu/kgeditor/User"
    )
  }

  def favoriteToNexusStruct(favorite: String, favoriteGroupNexusId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(favoriteGroupNexusId + favorite),
      "http://hbp.eu/kgeditor/favoriteGroup" -> Json.obj("@id" -> s"$favoriteGroupNexusId"),
      "http://hbp.eu/kgeditor/favoriteInstanceLink" -> Json.obj("@id" -> s"$favorite"),
      "http://hbp.eu/kgeditor/favoriteInstance" -> s"${favorite.split("/v0/data/").last}",
      "@type" -> "http://hbp.eu/kgeditor/Favorite"
    )
  }

  def favoriteGroupToNexusStruct(name:String, userNexusId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userNexusId + name),
      "http://schema.org/name" -> name,
      "http://hbp.eu/kgeditor/user" -> Json.obj("@id" -> s"$userNexusId"),
      "@type" -> "http://hbp.eu/kgeditor/FavoriteGroup"
    )
  }
}
