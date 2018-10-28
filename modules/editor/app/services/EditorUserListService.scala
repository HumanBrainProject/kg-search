
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
import common.models.{NexusInstance, NexusUser}
import common.services.ConfigurationService
import editor.models.EditorUser
import editor.models.EditorUserList._
import editor.services.EditorUserService.editorUserPath
import helpers.ResponseHelper
import nexus.services.NexusService
import play.api.Logger
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.ContentTypes._
import play.api.http.Status._
import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}
import services.FormService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EditorUserListService @Inject()(config: ConfigurationService,
                                      wSClient: WSClient,
                                      formService: FormService,
                                      editorUserService: EditorUserService,
                                      oIDCAuthService: OIDCAuthService,
                                      nexusService: NexusService
                                 )(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  def getUserLists(nexusUser: NexusUser, user: EditorUser): Future[Either[WSResponse, List[UserFolder]]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${user.nexusId}")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorUserListService.kgQueryGetUserFoldersQuery).map {
      res =>
        res.status match {
          case OK =>
            val folders = (res.json \ "userFolders").as[List[UserFolder]]
            // Concatenate with form service lists
            val r = getEditableEntities(nexusUser)
            Right(folders ::: r)
          case _ =>
            logger.error(s"Could not fetch the user with ID ${user.id} ${res.body}")
            Left(res)
        }
    }


  }

  private def getEditableEntities(nexusUser: NexusUser) = {
    val allEditableEntities = FormService.editableEntities(nexusUser, formService.formRegistry)
      .foldLeft((List[UserInstanceList](), List[UserInstanceList]())) {
        case (acc, userList) =>
          if (EditorUserListService.commonNodeTypes.contains(userList.id)) {
            (userList :: acc._1, acc._2)
          } else {
            (acc._1, userList :: acc._2)
          }
      }
    List(
      UserFolder(
        "commonNodeTypes",
        "Common node types",
        NODETYPE,
        allEditableEntities._1
      ),
      UserFolder(
        "otherNodeTypes",
        "Other node types",
        NODETYPE,
        allEditableEntities._2
      )
    )

  }

  def createUserFolder(user: EditorUser, name: String, folderType: FolderType = BOOKMARK, token: String): Future[Option[UserFolder]] = {
    nexusService.createSimpleSchema(
      config.nexusEndpoint,
      EditorUserService.userFolderPath,
      token,
      Some(EditorUserService.editorNameSpace)
    ).flatMap { res =>
      res.status match {
        case OK | CREATED | CONFLICT =>
          val payload = EditorUserService.userFolderToNexusStruct(name, s"${config.nexusEndpoint}/v0/data/${user.nexusId}", folderType)
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorUserService.userFolderPath,
            payload,
            token
          ).map {
            res =>
              res.status match {
                case CREATED =>
                  val (id, path) = NexusInstance.extractIdAndPath(res.json)
                  Some(UserFolder(s"${path.toString()}/$id", name, folderType, List()))
                case _ =>
                  logger.error("Error while creating a user folder " + res.body)
                  None
              }
          }
        case _ =>
          logger.error("Could created schema for User folder")
          Future(None)
      }
    }
  }
}

object EditorUserListService {
  val commonNodeTypes = List("minds/core/dataset/v0.0.4")

  val kgQueryGetUserFoldersQuery = s"""
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
     |        "fieldname": "userFolders",
     |        "relative_path": {
     |            "@id": "kgeditor:user",
     |            "reverse":true
     |        },
     |        "fields": [
     |           {
     |             "fieldname": "folderName",
     |             "relative_path": "schema:name",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "id",
     |             "relative_path": "@id",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "folderType",
     |             "relative_path": "kgeditor:folderType",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "lists",
     |             "required": true,
     |             "relative_path": {
     |                 "@id": "kgeditor:userFolder",
     |                 "reverse":true
     |               },
     |               "fields":[
     |               		{
     |               			"fieldname":"id",
     |               			"relative_path": "@id"
     |               		}
     |              ]
     |          }
     |        ]
     |     }
     |  ]
     |}
    """.stripMargin
}
