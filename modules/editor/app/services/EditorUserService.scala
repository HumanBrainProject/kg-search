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
import constants.{EditorConstants, InternalSchemaFieldsConstants, SchemaFieldsConstants}
import models.errors.APIEditorError
import models._
import models.instance.{NexusInstance, NexusInstanceReference}
import models.user.{EditorUser, NexusUser}
import play.api.Logger
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.http.ContentTypes._
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import services.instance.InstanceApiService
import services.query.QueryService

import scala.concurrent.{ExecutionContext, Future}

class EditorUserService @Inject()(config: ConfigurationService,
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

  def getUser(nexusUser: NexusUser): Future[Either[APIEditorError, EditorUser]] = {
    cacheService.getOrElse[EditorUser](cache, nexusUser.id.toString){
      queryService.getInstances(
        wSClient,
        config.kgQueryEndpoint,
        EditorConstants.editorUserPath,
        EditorUserService.kgQueryGetUserQuery(EditorConstants.editorUserPath)
      ).map{
        res => res.status match {
          case OK => (res.json \ "results").as[List[JsObject]]
            .find(js => (js \ "userId").asOpt[String].getOrElse("") == nexusUser.id)
            .map{js =>
              val id = (js \ "nexusId").as[String]
              EditorUser(NexusInstanceReference.fromUrl(id) , nexusUser)
            }
          case _ =>
            logger.error(s"Could not fetch the user with ID ${nexusUser.id} ${res.body}")
            None
        }
      }
    }.map{
      case Some(editorUser) =>
        cache.set(editorUser.nexusUser.id, editorUser, config.cacheExpiration)
        Right(editorUser)
      case None => Left(APIEditorError(NOT_FOUND, "Could not find editor user"))
    }
  }


  def createUser(nexusUser: NexusUser, token: String): Future[Either[WSResponse, EditorUser]] = {
    instanceApiService.post(
      wSClient,
      config.kgQueryEndpoint,
      NexusInstance(None, EditorConstants.editorUserPath, EditorUserService.userToNexusStruct(nexusUser.id)),
      token
    ).map {
      case Right(ref) =>
        Right(EditorUser(ref, nexusUser))
      case Left(res) => Left(res)
    }
  }
}

object EditorUserService {


  def kgQueryGetUserQuery (editorUserPath: NexusPath, context: String = EditorConstants.context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${editorUserPath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname": "nexusId",
       |      "required": true,
       |      "relative_path": "base:${EditorConstants.RELATIVEURL}"
       |    },
       |    {
       |      "fieldname": "userId",
       |      "required": true,
       |      "relative_path": "hbpkg:${EditorConstants.USERID}"
       |    }
       |  ]
       |}
    """.stripMargin




  def userToNexusStruct(userId: String): JsObject = {
    Json.obj(
      EditorConstants.EDITORNAMESPACE + EditorConstants.USERID -> userId
    )
  }

}
