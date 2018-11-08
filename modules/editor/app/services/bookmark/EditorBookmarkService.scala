
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
import constants.{EditorConstants, SchemaFieldsConstants}
import models.errors.APIEditorError
import helpers.InstanceHelper
import models._
import models.editorUserList._
import models.instance.{NexusInstance, NexusInstanceReference, PreviewInstance}
import models.user.{EditorUser, NexusUser}
import play.api.Logger
import play.api.http.ContentTypes._
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import services.NexusService.{SKIP, UPDATE}
import services._

import scala.concurrent.{ExecutionContext, Future}

class EditorBookmarkService @Inject()(config: ConfigurationService,
                                      wSClient: WSClient,
                                      nexusService: NexusService,
                                      nexusExtensionService: NexusExtensionService
                                 )(implicit executionContext: ExecutionContext) extends EditorBookmarkServiceInterface {
  val logger = Logger(this.getClass)

  def getUserLists(editorUser: EditorUser, formRegistry: FormRegistry): Future[Either[WSResponse, List[BookmarkListFolder]]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${editorUser.nexusId}")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetUserFoldersQuery(EditorUserService.editorUserPath)).map {
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
          if (EditorConstants.commonNodeTypes.contains(userList.id)) {
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

  def getInstancesOfBookmarkList(bookmarkListId: String, start:Int, size:Int, search:String):Future[Either[WSResponse, (List[PreviewInstance], Long)]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/arango/bookmarks/${bookmarkListId}")
      .withQueryStringParameters( "from" -> start.toString, "size" -> size.toString, "search" -> search)
      .withHttpHeaders(CONTENT_TYPE -> JSON)
//      .post(EditorBookmarkService.kgQueryGetInstances(EditorConstants.bookmarkPath))
      .get()
      .map {
      res =>
        res.status match {
          case OK =>
            val instances = (res.json \ "data").as[List[PreviewInstance]]
            val total = (res.json \ "count").as[Long]
//              .filter(js => (js \ "bookmarkListId" \ "id").as[String].split("v0/data/").last == bookmarkListId)
//              .flatMap(js => (js \ "instances").asOpt[List[PreviewInstance]].getOrElse(List()))
            Right( (instances, total))
          case _ => Left(res)
        }
    }
  }

  def createBookmarkListFolder(user: EditorUser, name: String, folderType: FolderType = BOOKMARKFOLDER, token: String): Future[Option[BookmarkListFolder]] = {
    nexusExtensionService.createSimpleSchema(
      EditorConstants.bookmarkListFolderPath,
      Some(config.editorSubSpace)
    ).flatMap {
      case Right(()) =>
        val payload = EditorBookmarkService.bookmarkListFolderToNexusStruct(name, s"${config.nexusEndpoint}/v0/data/${user.nexusId}", folderType)
        nexusService.insertInstance(
          config.nexusEndpoint,
          EditorConstants.bookmarkListFolderPath.withSpecificSubspace(config.editorSubSpace),
          payload,
          token
        ).map {
          res =>
            res.status match {
              case CREATED =>
                val ref = NexusInstanceReference.fromUrl( (res.json \ "@id").as[String])
                Some(BookmarkListFolder(s"${ref.toString}", name, folderType, List()))
              case _ =>
                logger.error("Error while creating a user folder " + res.body)
                None
            }
        }
      case Left(res) =>
        logger.error(s"Could not created schema for User folder ${res.body}")
        Future(None)
    }
  }

  def createBookmarkList(bookmarkListName: String, folderId: String, token: String): Future[Either[WSResponse, BookmarkList]] = {
    nexusExtensionService.createSimpleSchema(
      EditorConstants.bookmarkListPath,
      Some(config.editorSubSpace)
    ).flatMap{
      case Right(()) =>
          val payload = EditorBookmarkService.bookmarkListToNexusStruct(bookmarkListName, s"${config.nexusEndpoint}/v0/data/${folderId}")
          nexusService.insertInstance(
            config.nexusEndpoint,
            EditorConstants.bookmarkListPath.withSpecificSubspace(config.editorSubSpace),
            payload,
            token
          ).map {
            res =>
              res.status match {
                case CREATED =>
                  val ref = NexusInstanceReference.fromUrl( (res.json \ "@id").as[String])
                  Right(BookmarkList(s"${EditorConstants.bookmarkListPath.toString()}/${ref.id}", bookmarkListName, None, None, None))
                case _ =>
                  logger.error("Error while creating a bookmark list " + res.body)
                  Left(res)
              }
          }
        case Left(res) =>
          logger.error("Could created schema for Bookmark list" + res.body)
          Future(Left(res))

    }
  }

  def getBookmarkListById(instancePath: NexusPath, instanceId: String): Future[Either[WSResponse, (BookmarkList, String)]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${instancePath.toString}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetBookmarkListByIdQuery(instancePath)).map {
        res =>
          res.status match {
            case OK =>
              val id = (res.json \ "id").as[String].split("/v0/data/").tail.head
              val name = (res.json \ "name").as[String]
              val bookmarkList = BookmarkList(id, name, None, None, None )
              val userFolderId = (res.json \ "userFolderId" \ "@id").as[String]
              Right((bookmarkList, userFolderId))
            case _ =>
              logger.error(s"Could not fetch the bookmark list  with ID ${instanceId} ${res.body}")
              Left(res)
          }
    }
  }

  def updateBookmarkList(
                          bookmarkList: BookmarkList,
                          bookmarkListPath: NexusPath,
                          bookmarkListId: String,
                          userFolderId: String,
                          token: String)
  : Future[Either[WSResponse, BookmarkList]] = {
    nexusService.updateInstance(
      s"${config.nexusEndpoint}/v0/data/${bookmarkListPath.toString()}/$bookmarkListId",
      None,
      EditorBookmarkService.bookmarkListToNexusStruct(bookmarkList.name, userFolderId), token
    ).map {
      res =>
        res._1 match {
          case UPDATE => Right(bookmarkList)
          case SKIP => Left(res._2)
        }
    }
  }


  def deleteBookmarkList(bookmarkListPath: NexusPath, instanceId: String, token: String): Future[Either[APIEditorError, Unit]] = {
    val pattern = """(.+)\?rev=(\d+)""".r
    def extractIdAndRev(s:String): (String, Long) = s match {
      case pattern(id, rev) => (id, rev.toLong)
      case id => (id, 1L)
    }
    // Get all bookmarks link to the bookmark list and their revision
    wSClient
      .url(s"${config.kgQueryEndpoint}/query/${bookmarkListPath.toString()}/$instanceId")
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .post(EditorBookmarkService.kgQueryGetBookmarksForDeletion(bookmarkListPath) ).flatMap {
      res =>
        res.status match {
          case OK =>
            val bookmarkListToDelete = extractIdAndRev((res.json \ "originalId").as[String])
            val bookmarksToDelete = (res.json \ "bookmarks").as[List[JsObject]].map{ js =>
              extractIdAndRev((js \ "originalId").as[String])
            }
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
                if(listOfResponse.forall(_.isRight)){
                  logger.debug("All the bookmarks are deleted. We can safely delete the bookmark list")
                  // Delete the bookmark list
                  nexusService.deprecateInstance(config.nexusEndpoint, bookmarkListPath.withSpecificSubspace(config.editorSubSpace), instanceId, bookmarkListToDelete._2, token).map {
                    case Right(()) => Right(())
                    case Left(r) => Left(APIEditorError(r.status, r.body))
                  }
                }else{
                  logger.error("Could not delete all the bookmarks")
                  val compiledMessage = listOfResponse
                    .filterNot(_.isRight)
                    .map{
                      case Left(res) =>  s"${res.status} - ${res.statusText} - ${res.body}"
                      case _ => ""
                    }.mkString("\n")
                  Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, compiledMessage)))
                }
            }
          case _ =>
            logger.error(s"Could not fetch the bookmarks to be deleted ${res.body}")
            Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch data to delete ")))
        }
    }
  }

  def addInstanceToBookmarkLists(
                                  instanceFullId: String,
                                  bookmarkListIds: List[String],
                                  token: String
                                ):
  Future[List[WSResponse]] = {
    nexusExtensionService.createSimpleSchema(
      EditorConstants.bookmarkPath,
      Some(config.editorSubSpace)
    ).flatMap {
        case Right(()) =>
          val queries = bookmarkListIds.map { id =>
            val toInsert = EditorBookmarkService.bookmarkToNexusStruct(instanceFullId, id)
            nexusService.insertInstance(
              config.nexusEndpoint,
              EditorConstants.bookmarkPath.withSpecificSubspace(config.editorSubSpace),
              toInsert,
              token
            )
          }
          Future.sequence(queries)
        case Left(res) =>
          logger.error("Could created schema for Bookmark" + res.body)
          Future(List(res))
    }
  }

  def removeInstanceFromBookmarkLists(
                                  instancePath: NexusPath,
                                  instanceId: String,
                                  bookmarkListIds: List[String],
                                  token: String
                                ):
  Future[List[Either[WSResponse, Unit]]] = {
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
                case None => Future(List(Left(res)))
            }
          case _ => Future(List(Left(res)))
        }

    }
  }

  def retrieveBookmarkList(instanceIds: List[(NexusPath, String)]): Future[List[(String, Either[APIEditorError, List[BookmarkList]])]] = {
    Future.sequence(instanceIds.map { ids =>
      retrieveBookmarkListSingleInstance(ids._1, ids._2)
    })
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
  def kgQueryGetUserFoldersQuery(userPath: NexusPath, context: String = context): String = s"""
     |{
     |  "@context": $context,
     |  "schema:name": "",
     |  "root_schema": "nexus_instance:${userPath.toString()}",
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
     |             "relative_path": "_originalId",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "folderType",
     |             "relative_path": "kgeditor:folderType",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "lists",
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

  def kgQueryGetInstanceBookmarks(instancePath: NexusPath,  context: String = context): String = s"""
    |{
    |  "@context": $context,
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

  def kgQueryGetInstanceBookmarkLists(instancePath: NexusPath,  context: String = context): String =
    s"""
    |{
    |  "@context": $context,
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

  def kgQueryGetInstances(bookmarkPath: NexusPath,  context: String = context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${bookmarkPath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname":"bookmarkListId",
       |      "relative_path":{
       |        "@id":"kgeditor:bookmarkList"
       |      },
       |      "fields":[
       |        {
       |          "fieldname":"id",
       |          "relative_path":"@id"
     |          }
       |       ]
       |    },
       |    {
       |               			"fieldname":"instances",
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
       |
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarkListByIdQuery(bookmarkListPath: NexusPath,  context: String = context): String =
    s"""
       |{
       |  "@context": $context,
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
       |       "fieldname":"userFolderId",
       |       "relative_path":"kgeditor:bookmarkListFolder"
       |     },
       |     {
       |       "fieldname":"_originalId",
       |       "relative_path":"_originalId"
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarksForDeletion(bookmarkListPath: NexusPath,  context: String = context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${bookmarkListPath.toString()}",
       |  "fields": [
       |  {
       |       "fieldname":"originalId",
       |       "relative_path": "_originalId"
       |       },
       |    {
       |        "fieldname": "bookmarks",
       |        "relative_path": {
       |            "@id": "kgeditor:bookmarkList",
       |            "reverse":true
       |        },
       |        "fields":[
       |               		{
       |               			"fieldname":"originalId",
       |               			"relative_path": "_originalId"
       |               		}
       |              ]
       |     }
       |  ]
       |}
     """.stripMargin

  def bookmarkToNexusStruct(bookmark: String, userBookMarkListNexusId: String): JsObject = {
    Json.obj(
      SchemaFieldsConstants.IDENTIFIER -> InstanceHelper.md5HashString(userBookMarkListNexusId + bookmark),
      EditorConstants.BOOKMARKLIST -> Json.obj("@id" -> s"$userBookMarkListNexusId"),
      EditorConstants.BOOKMARKINSTANCELINK -> Json.obj("@id" -> s"$bookmark"),
      "@type" -> s"${EditorConstants.EDITORNAMESPACE}Bookmark"
    )
  }

  def bookmarkListToNexusStruct(name:String, userFolderId: String): JsObject = {
    Json.obj(
      SchemaFieldsConstants.IDENTIFIER -> InstanceHelper.md5HashString(userFolderId + name),
      SchemaFieldsConstants.NAME -> name,
      EditorConstants.BOOKMARKLISTFOLDER -> Json.obj("@id" -> s"$userFolderId"),
      "@type" -> s"${EditorConstants.EDITORNAMESPACE}Bookmarklist"
    )
  }

  def bookmarkListFolderToNexusStruct(name:String, userNexusId: String, folderType: FolderType): JsObject = {
    Json.obj(
      SchemaFieldsConstants.IDENTIFIER -> InstanceHelper.md5HashString(userNexusId + name),
      SchemaFieldsConstants.NAME -> name,
      EditorConstants.USER -> Json.obj("@id" -> s"$userNexusId"),
      EditorConstants.FOLDERTYPE -> folderType.t,
      "@type" -> s"${EditorConstants.EDITORNAMESPACE}Bookmarklistfolder"
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
