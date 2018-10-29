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
import common.models._
import common.services.ConfigurationService
import editor.helpers.InstanceHelper
import editor.models.EditorUser
import editor.models.EditorUserList.FolderType
import helpers.ResponseHelper
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsObject, Json}
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

  def getUser(nexusUser: NexusUser): Future[Option[EditorUser]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorUserService.kgQueryGetUserQuery).map{
      res => res.status match {
        case OK => (res.json \ "results").as[List[JsObject]]
          .find(js => (js \ "userId").asOpt[String].getOrElse("") == nexusUser.id)
          .map(js => EditorUser( (js \ "nexusId").as[String], nexusUser))
        case _ =>
          logger.error(s"Could not fetch the user with ID $nexusUser.id " + res.body)
          None
      }
    }
  }


  def createUser(nexusUser: NexusUser, token: String): Future[Option[EditorUser]] = {
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
            EditorUserService.userToNexusStruct(nexusUser.id),
            token
          ).map { res =>
            res.status match {
              case OK | CREATED =>
                val (id, path) = NexusInstance.extractIdAndPath(res.json)
                Some(EditorUser(s"${path.toString}/$id", nexusUser))
              case _ => None
            }
          }
        case _ =>
          logger.error("Could not create editor User schema")
          Future(None)
      }
    }

  }
}

object EditorUserService {
  val editorUserPath = NexusPath("kgeditor", "core", "user", "v0.0.1")
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

}
