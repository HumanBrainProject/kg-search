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
import common.models.{EditorUser, NexusPath}
import helpers.ResponseHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class EditorUserService @Inject()(configuration: Configuration, wSClient: WSClient, nexusService: NexusService, oIDCAuthService: OIDCAuthService)(implicit executionContext: ExecutionContext) {
  val nexusEndpoint: String = configuration.getOptional[String]("nexus.endpoint").getOrElse("https://nexus-dev.humanbrainproject.org")
  val kgQueryEndpoint: String = configuration.getOptional[String]("kgquery.endpoint").getOrElse("http://localhost:8600")
  val logger = Logger(this.getClass)

  def getUser(id: String): Future[Option[EditorUser]] = {
    wSClient.url(s"$kgQueryEndpoint/query/").post(EditorUserService.kgQueryGetQuery()).map{
      res => res.status match {
        case OK => (res.json \ "results").as[List[EditorUser]].headOption
        case _ => None
      }
    }
  }

  def createUser(user: EditorUser): Future[WSResponse] = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>
        // Create the user in the nexus
        nexusService.insertInstance(
          nexusEndpoint,
          EditorUserService.editorUserPath.org,
          EditorUserService.editorUserPath.domain,
          EditorUserService.editorUserPath.schema,
          EditorUserService.editorUserPath.version,
          EditorUserService.userToNexusStruct(user),
          token
        ).flatMap{
          res =>
            res.status match {
              case OK | CREATED =>
                val userId = (res.json \ "@id").as[String]
                // Create all the favorite instance
                Future.sequence(
                  user.favorites.map { f =>
                    val payload = EditorUserService.favoriteToNexusStruct(s"$nexusEndpoint/v0/data/$f", userId)
                    nexusService.insertInstance(
                      nexusEndpoint,
                      EditorUserService.favoritePath.org,
                      EditorUserService.favoritePath.domain,
                      EditorUserService.favoritePath.schema,
                      EditorUserService.favoritePath.version,
                      payload,
                      token
                    )
                  }
                ).map{resList =>
                  val (successes, failures) = resList.span(el => el.status == OK || el.status == CREATED)
                  if(failures.isEmpty) {
                    successes.head
                  } else {
                    failures.foreach(el => logger.error(el.body))
                    failures.head
                  }
                }
              case _ => Future {res}
            }
        }
    }
  }

  def addFavorite(userId: String, instance: String): Future[WSResponse] = {
    oIDCAuthService.getTechAccessToken().flatMap {
      token =>
        val payload = EditorUserService.favoriteToNexusStruct(s"$nexusEndpoint/v0/data/$instance", userId)
        nexusService.insertInstance(
          nexusEndpoint,
          EditorUserService.favoritePath.org,
          EditorUserService.favoritePath.domain,
          EditorUserService.favoritePath.schema,
          EditorUserService.favoritePath.version,
          payload,
          token
        )
    }
  }

//  def removeFavorite(userId: String, instance: String): Future[WSResponse] = {
//    oIDCAuthService.getTechAccessToken().flatMap {
//      token =>
//        val payload = EditorUserService.favoriteToNexusStruct(s"$nexusEndpoint/v0/data/$instance", userId)
//        nexusService.deprecateInstance(
//          nexusEndpoint,
//          EditorUserService.favoritePath.org,
//          EditorUserService.favoritePath.domain,
//          EditorUserService.favoritePath.schema,
//          EditorUserService.favoritePath.version,
//          payload,
//          token
//        )
//    }
//  }

  def getOrCreate(user: EditorUser): Future[Option[EditorUser]] = {
    this.getUser(user.id).flatMap{
      case None => this.createUser(user).map{ res =>
        res.status match {
          case OK | CREATED => Some(user)
          case _ => None
        }
      }
      case Some(u) => Future{Some(u)}
    }
  }
}

object EditorUserService {
  val editorUserPath = NexusPath("kgeditor", "core", "user", "v0.0.2")
  val favoritePath = NexusPath("kgeditor", "core", "favorite", "v0.0.1")

  def kgQueryGetQuery() =
    """
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
      |  "root_schema": "nexus_instance:${instance}",
      |  "fields": [
      |    {
      |      "fieldname": "id",
      |      "required": true,
      |      "relative_path": "schema:identifier"
      |    },
      |    {
      |        "fieldname": "favorites",
      |        "relative_path": [
      |            {
      |                "@id": "kgeditor:user",
      |                "reverse":true
      |            },
      |            "kgeditor:instance"
      |        ]
      |    }
      |  ]
      |}
    """.stripMargin

  def userToNexusStruct(user: EditorUser) = {
    Json.obj(
      "http://schema.org/identifier" -> user.id,
      "@type" -> "http://hbp.eu/kgeditor/User"
    )
  }

  def favoriteToNexusStruct(favorite: String, userNexusId: String) = {
    Json.obj(
      "http://hbp.eu/kgeditor/user" -> Json.obj("@id" -> s"$userNexusId"),
      "http://hbp.eu/kgeditor/instance" -> Json.obj("@id" -> s"$favorite"),
      "@type" -> "http://hbp.eu/kgeditor/Favorite"
    )
  }
}
