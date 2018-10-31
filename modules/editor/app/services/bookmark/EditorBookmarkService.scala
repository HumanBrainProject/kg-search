
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
package services.bookmark

import com.google.inject.Inject
import helpers.InstanceHelper
import models._
import models.editorUserList._
import models.instance.{NexusInstance, PreviewInstance}
import models.user.{EditorUser, NexusUser}
import play.api.Logger
import play.api.http.ContentTypes._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import services.EditorUserService.{editorNameSpace, editorUserPath}
import services.NexusService.{SKIP, UPDATE}
import services._

import scala.concurrent.{ExecutionContext, Future}

class EditorBookmarkService @Inject()(config: ConfigurationService,
                                      wSClient: WSClient,
                                      nexusService: NexusService
                                 )(implicit executionContext: ExecutionContext) extends EditorBookmarkServiceInterface {
  val logger = Logger(this.getClass)

  def getUserLists(editorUser: EditorUser, formRegistry: FormRegistry): Future[Either[WSResponse, List[BookmarkListFolder]]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${editorUser.nexusId}")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetUserFoldersQuery).map {
      res =>
        res.status match {
          case OK =>
            val folders = (res.json \ "userFolders").as[List[BookmarkListFolder]]
            // Concatenate with form service lists
            val r = getEditableEntities(editorUser.nexusUser, formRegistry)
            Right(folders ::: r)
          case _ =>
            logger.error(s"Could not fetch the user with ID ${editorUser.nexusUser.id} ${res.body}")
            Left(res)
        }
    }
  }

  /**
    * Get folder containing static list of bookmarklist
    * @param nexusUser Nexus user is needed in order to retriev correct access right to entity types
    * @param formRegistry The form containing all the entities information
    * @return a list of bookmark folder
    */
  private def getEditableEntities(nexusUser: NexusUser, formRegistry: FormRegistry): List[BookmarkListFolder] = {
    val allEditableEntities = FormService.editableEntities(nexusUser, formRegistry)
      .foldLeft((List[BookmarkList](), List[BookmarkList]())) {
        case (acc, userList) =>
          if (EditorBookmarkService.commonNodeTypes.contains(userList.id)) {
            (userList :: acc._1, acc._2)
          } else {
            (acc._1, userList :: acc._2)
          }
      }
    List(
      BookmarkListFolder(
        "commonNodeTypes",
        "Common node types",
        NODETYPEFOLDER,
        allEditableEntities._1
      ),
      BookmarkListFolder(
        "otherNodeTypes",
        "Other node types",
        NODETYPEFOLDER,
        allEditableEntities._2
      )
    )

  }

  def getInstanceOfBookmarkList(bookmarkListId: String, start:Int, size:Int, search:String):Future[Either[WSResponse, List[PreviewInstance]]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/arango/bookmarkList/${bookmarkListId}")
      .withQueryStringParameters( "start" -> start.toString, "size" -> size.toString, "search" -> search)
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .get().map {
      res =>
        res.status match {
          case OK =>
            Right(res.json.asOpt[List[PreviewInstance]].getOrElse(List()))
          case _ => Left(res)
        }
    }
  }

  def createBookmarkListFolder(user: EditorUser, name: String, folderType: FolderType = BOOKMARKFOLDER, token: String): Future[Option[BookmarkListFolder]] = {
    nexusService.createSimpleSchema(
      config.nexusEndpoint,
      EditorBookmarkService.bookmarkListFolderPath,
      token,
      Some(EditorUserService.editorNameSpace)
    ).flatMap { res =>
      res.status match {
        case OK | CREATED | CONFLICT =>
          val payload = EditorBookmarkService.bookmarkListFolderToNexusStruct(name, s"${config.nexusEndpoint}/v0/data/${user.nexusId}", folderType)
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorBookmarkService.bookmarkListFolderPath,
            payload,
            token
          ).map {
            res =>
              res.status match {
                case CREATED =>
                  val (id, path) = NexusInstance.extractIdAndPath(res.json)
                  Some(BookmarkListFolder(s"${path.toString()}/$id", name, folderType, List()))
                case _ =>
                  logger.error("Error while creating a user folder " + res.body)
                  None
              }
          }
        case _ =>
          logger.error("Could not created schema for User folder")
          Future(None)
      }
    }
  }

  def createBookmarkList(bookmarkListName: String, folderId: String, token: String): Future[Either[WSResponse, BookmarkList]] = {
    nexusService.createSimpleSchema(
      config.nexusEndpoint,
      EditorBookmarkService.bookmarkListPath,
      token,
      Some(EditorUserService.editorNameSpace)
    ).flatMap{ res =>
      res.status match {
        case OK | CREATED | CONFLICT =>
          val payload = EditorBookmarkService.bookmarkListToNexusStruct(bookmarkListName, s"${config.nexusEndpoint}/v0/data/${folderId}")
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorBookmarkService.bookmarkListPath,
            payload,
            token
          ).map {
            res =>
              res.status match {
                case CREATED =>
                  val (id, path) = NexusInstance.extractIdAndPath(res.json)
                  Right(BookmarkList(s"${path.toString()}/$id", bookmarkListName, None, None, None))
                case _ =>
                  logger.error("Error while creating a bookmark list " + res.body)
                  Left(res)
              }
          }
        case _ =>
          logger.error("Could created schema for Bookmark list" + res.body)
          Future(Left(res))
      }

    }
  }

  def getBookmarkListById(instancePath: NexusPath, instanceId: String): Future[Either[WSResponse, (BookmarkList, Long, String)]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${instancePath.toString}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetBookmarkListByIdQuery(instancePath)).map {
        res =>
          res.status match {
            case OK =>
              val bookmarkList = res.json.as[BookmarkList]
              val rev = (res.json \ "revision").as[Long]
              val userFolderId = (res.json \ "userFolderId" \ "@id").as[String]
              Right((bookmarkList, rev, userFolderId))
            case _ =>
              logger.error(s"Could not fetch the bookmark list  with ID ${instanceId} ${res.body}")
              Left(res)
          }
    }
  }

  def updateBookmarkList(bookmarkList: BookmarkList, userFolderId:String, revision: Long, token: String): Future[Either[WSResponse, BookmarkList]] = {
    nexusService.updateInstance(
      s"${config.nexusEndpoint}/v0/data/${bookmarkList.id}",
      Some(revision),
      EditorBookmarkService.bookmarkListToNexusStruct(bookmarkList.name, userFolderId), token
    ).map{
      res => res._1 match {
        case UPDATE => Right(bookmarkList)
        case SKIP => Left(res._2)
      }
    }
  }


  def deleteBookmarkList(bookmarkListPath: NexusPath, instanceId: String, token: String): Future[Either[APIEditorError, Unit]] = {
    // Get all bookmarks link to the bookmark list and their revision
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${bookmarkListPath.toString}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetBookmarksForDeletion ).flatMap {
      res =>
        res.status match {
          case OK =>
            val bookmarkListToDelete = ((res.json \ "id").as[String] , (res.json \ "rev").as[Long])
            val bookmarksToDelete = (res.json \ "bookmarks").as[List[JsObject]].map( js => ((js \ "id").as[String], (js \ "rev").as[Long] ))
          // Delete all bookmarks
            val listOfFuture = for {
              (identifier, rev) <- bookmarksToDelete
            } yield {
              val (pathAsString, id) = identifier.splitAt(identifier.lastIndexOf("/"))
              val path = NexusPath(pathAsString)
              val cleanId = id.replaceFirst("/", "")
              nexusService.deprecateInstance(config.nexusEndpoint, path, cleanId, rev, token)
            }
            Future.sequence(listOfFuture).flatMap[Either[APIEditorError, Unit]]{
              listOfResponse =>
                if(listOfResponse.forall(res => res.status == OK)){
                  logger.debug("All the bookmarks are deleted. We can safely delete the bookmark list")
                  // Delete the bookmark list
                  nexusService.deprecateInstance(config.nexusEndpoint, bookmarkListPath, instanceId, bookmarkListToDelete._2, token).map {
                    res =>
                      res.status match {
                        case OK => Right(())
                        case _ => Left(APIEditorError(res.status, res.body))
                      }
                  }
                }else{
                  logger.error("Could not delete all the bookmarks")
                  val compiledMessage = listOfResponse
                    .filterNot(res => res.status == OK)
                    .map(res => s"${res.status} - ${res.statusText} - ${res.body}").mkString("\n")
                  Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, compiledMessage)))
                }
            }
          case _ =>
            logger.error(s"Could not fetch the bookmarks to be deleted ${res.body}")
            Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch delete the data")))

        }
    }
  }

  def addInstanceToBookmarkLists(
                                  instanceFullId: String,
                                  bookmarkListIds: List[String],
                                  token: String
                                ):
  Future[List[WSResponse]] = {
    nexusService.createSimpleSchema(
      config.nexusEndpoint,
      EditorBookmarkService.bookmarkPath,
      token,
      Some(EditorUserService.editorNameSpace)
    ).flatMap { res =>
      res.status match {
        case OK | CREATED | CONFLICT =>
          val queries = bookmarkListIds.map { id =>
            val toInsert = EditorBookmarkService.bookmarkToNexusStruct(instanceFullId, id)
            nexusService.insertInstance(
              config.nexusEndpoint,
              EditorBookmarkService.bookmarkPath,
              toInsert,
              token
            )
          }
          Future.sequence(queries)
        case _ =>
          logger.error("Could created schema for Bookmark" + res.body)
          Future(List(res))
      }
    }
  }

  def removeInstanceFromBookmarkLists(
                                  instancePath: NexusPath,
                                  instanceId: String,
                                  bookmarkListIds: List[String],
                                  token: String
                                ):
  Future[List[WSResponse]] = {
    // Get the ids of the bookmarks
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${instancePath.toString()}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetInstanceBookmarks(instancePath)).flatMap {
      res =>
        res.status match {
          case OK =>
              (res.json \ "bookmarks" ).asOpt[List[JsValue]] match{
                case Some(ids) => //Delete the bookmarks
                  val listResponses = ids
                    .filter(id => bookmarkListIds
                      .exists( bookmarkListId => (id \ "bookmarkListId" \ "@id").asOpt[String].getOrElse("").contains(bookmarkListId) )
                    )
                    .map { id =>
                      val str = (id \ "id").as[String]
                      val (path, nexusId) = str.splitAt(str.lastIndexOf("/"))
                      nexusService.deprecateInstance(config.nexusEndpoint,NexusPath(path), nexusId.substring(1), 1L, token)
                    }
                  Future.sequence(listResponses)
                case None => Future(List(res))
            }
          case _ => Future(List(res))
        }

    }
  }

  def retrieveBookmarkList(instanceIds: List[(NexusPath, String)]): Future[Map[String, Either[APIEditorError, List[BookmarkList]]]] = {
    Future.sequence(instanceIds.map { ids =>
      retrieveBookmarkListSingleInstance(ids._1, ids._2)
    }).map(_.toMap)
  }

  private def retrieveBookmarkListSingleInstance(instancePath: NexusPath, instanceId: String): Future[(String ,Either[APIEditorError, List[BookmarkList]])] = {
    val id = s"${instancePath.toString()}/$instanceId"
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${instancePath.toString()}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetInstanceBookmarkLists(instancePath)).map {
      res =>
        res.status match {
          case OK =>
            (res.json \ "result").asOpt[List[JsValue]] match {
              case Some(result) =>
                (id,  Right(result.headOption.map{ js => (js \ "bookmarkList").as[List[BookmarkList]]}.getOrElse(List())))
              case None => (id, Left(APIEditorError(NOT_FOUND, "No result found")))
            }
          case _ => (id, Left(APIEditorError(res.status, res.body)))
        }
    }
  }

}

object EditorBookmarkService {
  val commonNodeTypes = List("minds/core/dataset/v0.0.4")
  val bookmarkListFolderPath = NexusPath("kgeditor", "core", "bookmarklistfolder", "v0.0.1")
  val bookmarkListPath = NexusPath("kgeditor", "core", "bookmarklist", "v0.0.1")
  val bookmarkPath = NexusPath("kgeditor", "core", "bookmark", "v0.0.1")

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
     |                 "@id": "kgeditor:bookmarkListFolder",
     |                 "reverse":true
     |               },
     |               "fields":[
     |               		{
     |               			"fieldname":"id",
     |               			"relative_path": "@id"
     |               		},
     |                  {
     |               			"fieldname":"name",
     |               			"relative_path": "schema:name"
     |               		}
     |              ]
     |          }
     |        ]
     |     }
     |  ]
     |}
    """.stripMargin

  def kgQueryGetInstanceBookmarks(instancePath: NexusPath): String = s"""
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
    |  "root_schema": "nexus_instance:${instancePath.toString()}",
    |  "fields": [
    |    {
    |        "fieldname": "bookmarks",
    |        "relative_path": {
    |            "@id": "kgeditor:bookmarkInstanceLink",
    |            "reverse":true
    |        },
    |        "fields":[
    |               		{
    |               			"fieldname":"id",
    |               			"relative_path": "@id"
    |               		},
    |               		{
    |               			"fieldname":"bookmarkListId",
    |               			"relative_path": "kgeditor:bookmarkList"
    |               		}
    |              ]
    |     }
    |  ]
    |}
    """.stripMargin

  def kgQueryGetInstanceBookmarkLists(instancePath: NexusPath): String =
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
    |  "root_schema": "nexus_instance:${instancePath.toString()}",
    |  "fields": [
    |    {
    |        "fieldname": "result",
    |        "relative_path": {
    |            "@id": "kgeditor:bookmarkInstanceLink",
    |            "reverse":true
    |        },
    |        "fields":[
    |               		{
    |               			"fieldname":"bookmarkList",
    |               			"relative_path": {
    |               				"@id":"kgeditor:bookmarkList"
    |               			},
    |
    |               			"fields":[
    |               				{
    |               					"fieldname":"name",
    |               					"relative_path":"schema:name"
    |               				},
    |               				{
    |               					"fieldname":"id",
    |               					"relative_path":"@id"
    |               				}
    |               				]
    |
    |
    |               		}
    |              ]
    |     }
    |  ]
    |}
    """.stripMargin

  def kgQueryGetInstances(bookmarkListPath: NexusPath): String =
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
       |  "root_schema": "nexus_instance:${bookmarkListPath.toString()}",
       |  "fields": [
       |    {
       |        "fieldname": "result",
       |        "relative_path": {
       |            "@id": "kgeditor:bookmarkList",
       |            "reverse":true
       |        },
       |        "fields":[
       |               		{
       |               			"fieldname":"instanceList",
       |               			"relative_path": {
       |               				"@id":"kgeditor:bookmarkInstanceLink"
       |               			},
       |
       |               			"fields":[
       |                         {
       |                           "fieldname":"description",
       |                           "relative_path":"schema:description"
       |                         },
       |               				{
       |               					"fieldname":"name",
       |               					"relative_path":"schema:name"
       |               				},
       |               				{
       |               					"fieldname":"id",
       |               					"relative_path":"@id"
       |               				}
       |               				]
       |               		}
       |              ]
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarkListByIdQuery(bookmarkListPath: NexusPath): String =
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
       |    "internal": "http://schema.hbp.eu/internal#",
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
       |  "root_schema": "nexus_instance:${bookmarkListPath.toString()}",
       |  "fields": [
       |     {
       |      "fieldname":"name",
       |      "relative_path":"schema:name"
       |     },
       |     {
       |        "fieldname":"id",
       |       "relative_path":"@id"
       |     },
       |     {
       |       "fieldname":"revision",
       |       "relative_path":"internal:rev"
       |     },
       |     {
       |       "fieldname":"userFolderId",
       |       "relative_path":"kgeditor:bookmarkListFolder"
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarksForDeletion: String =
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
       |    "internal": "http://schema.hbp.eu/internal#",
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
       |  "root_schema": "nexus_instance:${bookmarkListPath.toString()}",
       |  "fields": [
       |  {
       |       "fieldname":"id",
       |       "relative_path": "@id"
       |       },
       |      {
       |        "fieldname":"rev",
       |       	"relative_path": "internal:rev"
       |      },
       |    {
       |        "fieldname": "bookmarks",
       |        "relative_path": {
       |            "@id": "kgeditor:bookmarkList",
       |            "reverse":true
       |        },
       |        "fields":[
       |               		{
       |               			"fieldname":"id",
       |               			"relative_path": "@id"
       |               		},
       |               		{
       |               			"fieldname":"rev",
       |               			"relative_path": "internal:rev"
       |               		}
       |              ]
       |     }
       |  ]
       |}
     """.stripMargin

  def bookmarkToNexusStruct(bookmark: String, userBookMarkListNexusId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userBookMarkListNexusId + bookmark),
      "http://hbp.eu/kgeditor/bookmarkList" -> Json.obj("@id" -> s"$userBookMarkListNexusId"),
      "http://hbp.eu/kgeditor/bookmarkInstanceLink" -> Json.obj("@id" -> s"$bookmark"),
      "@type" -> s"${editorNameSpace}Bookmark"
    )
  }

  def bookmarkListToNexusStruct(name:String, userFolderId: String) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userFolderId + name),
      "http://schema.org/name" -> name,
      "http://hbp.eu/kgeditor/bookmarkListFolder" -> Json.obj("@id" -> s"$userFolderId"),
      "@type" -> s"${editorNameSpace}Bookmarklist"
    )
  }

  def bookmarkListFolderToNexusStruct(name:String, userNexusId: String, folderType: FolderType) = {
    Json.obj(
      "http://schema.org/identifier" -> InstanceHelper.md5HashString(userNexusId + name),
      "http://schema.org/name" -> name,
      "http://hbp.eu/kgeditor/user" -> Json.obj("@id" -> s"$userNexusId"),
      "http://hbp.eu/kgeditor/folderType" -> folderType.t,
      "@type" -> s"${editorNameSpace}Bookmarklistfolder"
    )
  }

  implicit object JsEither {
    implicit def eitherReads[A, B](implicit A: Reads[A], B: Reads[B]): Reads[Either[A, B]] =
      Reads[Either[A, B]] { json =>
        A.reads(json) match {
          case JsSuccess(value, path) => JsSuccess(Left(value), path)
          case JsError(e1) => B.reads(json) match {
            case JsSuccess(value, path) => JsSuccess(Right(value), path)
            case JsError(e2) => JsError(JsError.merge(e1, e2))
          }
        }
      }

    implicit def eitherWrites[A, B](implicit A: Writes[A], B: Writes[B]): Writes[Either[A,B]] =
      Writes[Either[A, B]] {
        case Left(a) => A.writes(a)
        case Right(b) => B.writes(b)
      }

    implicit def eitherFormat[A, B](implicit A: Format[A], B: Format[B]): Format[Either[A,B]] =
      Format(eitherReads, eitherWrites)
  }
}
