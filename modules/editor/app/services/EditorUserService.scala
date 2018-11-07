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
import constants.{EditorConstants, SchemaFieldsConstants}
import models.errors.APIEditorError
import models._
import models.instance.NexusInstance
import models.user.{EditorUser, NexusUser}
import play.api.Logger
import play.api.http.ContentTypes._
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class EditorUserService @Inject()(config: ConfigurationService,
                                  wSClient: WSClient,
                                  nexusService: NexusService,
                                  nexusExtensionService: NexusExtensionService,
                                  oIDCAuthService: OIDCAuthService,
                                 )(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  def getUser(nexusUser: NexusUser): Future[Either[APIEditorError, EditorUser]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorUserService.kgQueryGetUserQuery(EditorUserService.editorUserPath)).map{
      res => res.status match {
        case OK => (res.json \ "results").as[List[JsObject]]
          .find(js => (js \ "userId").asOpt[String].getOrElse("") == nexusUser.id)
          .map{js =>
            val id = (js \ "nexusId").as[String].split("/").last
            Right(EditorUser( s"${EditorUserService.editorUserPath.toString()}/$id" , nexusUser))
          }.getOrElse(Left(APIEditorError(NOT_FOUND, "Could not find editor user")))
        case _ =>
          logger.error(s"Could not fetch the user with ID ${nexusUser.id} " + res.body)
          Left(APIEditorError(NOT_FOUND, "Could not find editor user"))
      }
    }
  }


  def createUser(nexusUser: NexusUser, token: String): Future[Either[WSResponse, EditorUser]] = {
    nexusExtensionService
      .createSimpleSchema(EditorUserService.editorUserPath, Some(config.editorSubSpace))
      .flatMap {
        case Right(()) =>
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorUserService.editorUserPath.withSpecificSubspace(config.editorSubSpace),
            EditorUserService.userToNexusStruct(nexusUser.id),
            token
          ).map { res =>
            res.status match {
              case OK | CREATED =>
                val (id, path) = NexusInstance.extractIdAndPath(res.json)
                 Right(EditorUser(s"${path.toString}/$id", nexusUser))
              case _ => Left(res)
            }
          }
        case Left(res) =>
          logger.error(s"Could not create editor User schema - ${res.body}")
          Future(Left(res))
      }
  }
}

object EditorUserService {
  val editorUserPath = NexusPath("kg", "core", "user", "v0.0.1")

  val context =
    s"""
      |{
      |    "@vocab": "https://schema.hbp.eu/graphQuery/",
      |    "schema": "http://schema.org/",
      |    "kgeditor": "${EditorConstants.EDITORNAMESPACE}",
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
      |  }
    """.stripMargin

  def kgQueryGetUserQuery (editorUserPath: NexusPath, context: String = context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${editorUserPath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname": "nexusId",
       |      "required": true,
       |      "relative_path": "_originalId"
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
      SchemaFieldsConstants.IDENTIFIER -> userId,
      "@type" -> s"${EditorConstants.EDITORNAMESPACE}User"
    )
  }

}
