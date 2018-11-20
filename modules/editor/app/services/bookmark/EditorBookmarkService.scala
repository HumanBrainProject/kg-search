
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
import helpers.{BookmarkHelper, InstanceHelper}
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
import services.instance.InstanceApiService
import services.query.QueryService

import scala.concurrent.{ExecutionContext, Future}

class EditorBookmarkService @Inject()(config: ConfigurationService,
                                      wSClient: WSClient,
                                      nexusService: NexusService,
                                      nexusExtensionService: NexusExtensionService
                                 )(implicit executionContext: ExecutionContext) extends EditorBookmarkServiceInterface {
  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService
  object queryService extends QueryService

  def getUserLists(editorUser: EditorUser, formRegistry: FormRegistry): Future[Either[APIEditorError, List[BookmarkListFolder]]] = {
    queryService.getInstancesWithId(
      wSClient,
      config.kgQueryEndpoint,
      editorUser.nexusId,
      EditorBookmarkService.kgQueryGetUserFoldersQuery(EditorConstants.editorUserPath)
    ).map {
      res =>
        res.status match {
          case OK =>
            val folders = (res.json \ "userFolders").as[List[BookmarkListFolder]]
            // Concatenate with form service lists
            val r = getEditableEntities(editorUser.nexusUser, formRegistry)
            Right(folders ::: r)
          case _ =>
            logger.error(s"Could not fetch the user with ID ${editorUser.nexusUser.id} ${res.body}")
            Left(APIEditorError(res.status, res.body))
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
        None,
        "Common node types",
        NODETYPEFOLDER,
        allEditableEntities._1.sortBy(b => b.name)
      ),
      BookmarkListFolder(
        None,
        "Other node types",
        NODETYPEFOLDER,
        allEditableEntities._2.sortBy(b => b.name)
      )
    )

  }

  def getInstancesOfBookmarkList(bookmarkListId: NexusInstanceReference, start:Int, size:Int, search:String):Future[Either[APIEditorError, (List[PreviewInstance], Long)]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/arango/bookmarks/${bookmarkListId.toString}")
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
            Right( (instances, total))
          case _ => Left(APIEditorError(res.status, res.body))
        }
    }
  }

  def createBookmarkListFolder(
                                user: EditorUser,
                                name: String,
                                folderType: FolderType = BOOKMARKFOLDER,
                                token: String
                              ): Future[Either[APIEditorError, BookmarkListFolder]] = {
    val payload = EditorBookmarkService.bookmarkListFolderToNexusStruct(name, s"${config.nexusEndpoint}/v0/data/${user.nexusId}", folderType)
    instanceApiService.post(
      wSClient,
      config.kgQueryEndpoint,
      NexusInstance(None, EditorConstants.bookmarkListFolderPath, payload),
      token
    ).map {
      case Right(ref) =>
        Right(BookmarkListFolder(Some(ref), name, folderType, List()))
      case Left(res) =>
        logger.error("Error while creating a user folder " + res.body)
        Left(APIEditorError(res.status, res.body))
    }
  }

  def createBookmarkList(bookmarkListName: String, folderId: String, token: String): Future[Either[APIEditorError, BookmarkList]] = {
    val payload = EditorBookmarkService.bookmarkListToNexusStruct(bookmarkListName, s"${config.nexusEndpoint}/v0/data/${folderId}")
    instanceApiService.post(
      wSClient,
      config.kgQueryEndpoint,
      NexusInstance(None, EditorConstants.bookmarkListPath, payload),
      token
    ).map {
      case Right(ref) =>
        Right(BookmarkList(ref.toString, bookmarkListName, None, None, None))
      case Left(res) =>
        logger.error("Error while creating a bookmark list " + res.body)
        Left(APIEditorError(res.status, res.body))
    }
  }

  def getBookmarkListById(instanceReference: NexusInstanceReference): Future[Either[APIEditorError, (BookmarkList, String)]] = {
    queryService.getInstancesWithId(
      wSClient,
      config.kgQueryEndpoint,
      instanceReference,
      EditorBookmarkService.kgQueryGetBookmarkListByIdQuery(instanceReference.nexusPath)
    ).map {
        res =>
          res.status match {
            case OK =>
              val id = NexusInstanceReference.fromUrl((res.json \ "id").as[String])
              val name = (res.json \ "name").as[String]
              val bookmarkList = BookmarkList(id.toString(), name, None, None, None )
              val userFolderId = (res.json \ "userFolderId" \ "@id").as[String]
              Right((bookmarkList, userFolderId))
            case _ =>
              logger.error(s"Could not fetch the bookmark list  with ID ${instanceReference.id} ${res.body}")
              Left(APIEditorError(res.status, res.body))
          }
    }
  }

  def updateBookmarkList(
                          bookmarkList: BookmarkList,
                          bookmarkListPath: NexusPath,
                          bookmarkListId: String,
                          userFolderId: String,
                          token: String)
  : Future[Either[APIEditorError, BookmarkList]] = {
    nexusService.updateInstance(
      s"${config.nexusEndpoint}/v0/data/${bookmarkListPath.toString()}/$bookmarkListId",
      None,
      EditorBookmarkService.bookmarkListToNexusStruct(bookmarkList.name, userFolderId), token
    ).map {
      res =>
        res._1 match {
          case UPDATE => Right(bookmarkList)
          case SKIP => Left(APIEditorError(res._2.status, res._2.body))
        }
    }
  }


  def deleteBookmarkList(bookmarkRef: NexusInstanceReference, token: String): Future[Either[APIEditorError, Unit]] = {
    queryService.getInstancesWithId(
      wSClient,
      config.kgQueryEndpoint,
      bookmarkRef,
      EditorBookmarkService.kgQueryGetBookmarksForDeletion(bookmarkRef.nexusPath)
    ).flatMap {
      res =>
        res.status match {
          case OK =>
            val bookmarksToDelete = (res.json \ "bookmarks").as[List[JsObject]].map{ js =>
              NexusInstanceReference.fromUrl((js \ "originalId").as[String])
            }
          // Delete all bookmarks
            val listOfFuture = for {
              ref <- bookmarksToDelete
            } yield {
              instanceApiService.delete(wSClient,config.kgQueryEndpoint,ref,token)
            }
            Future.sequence(listOfFuture).flatMap[Either[APIEditorError, Unit]]{
              listOfResponse =>
                if(listOfResponse.forall(_.isRight)){
                  logger.debug("All the bookmarks are deleted. We can safely delete the bookmark list")
                  // Delete the bookmark list
                  instanceApiService.delete(
                    wSClient,
                    config.kgQueryEndpoint,
                    bookmarkRef,
                    token
                  ).map {
                    case Right(()) => Right(())
                    case Left(r) => Left(APIEditorError(r.status, r.body))
                  }
                }else{
                  logger.error("Could not delete all the bookmarks")
                  val compiledMessage = listOfResponse
                    .filterNot(_.isRight)
                    .map{
                      case Left(response) =>  s"${response.status} - ${response.statusText} - ${response.body}"
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
                                  instanceReference: NexusInstanceReference,
                                  bookmarkListIds: List[NexusInstanceReference],
                                  token: String
                                ):
  Future[List[Either[APIEditorError, NexusInstanceReference]]] = {
    val queries = bookmarkListIds.map { ref =>
      val toInsert = EditorBookmarkService
        .bookmarkToNexusStruct(s"${config.nexusEndpoint}/v0/data/${instanceReference.toString}", s"${config.nexusEndpoint}/v0/data/${ref.toString}")
      instanceApiService.post(
        wSClient,
        config.kgQueryEndpoint,
        NexusInstance(None, EditorConstants.bookmarkPath, toInsert),
        token
      ).map{
        case Left(res) => Left(APIEditorError(res.status, res.body))
        case Right(s) => Right(s)
      }
    }
    Future.sequence(queries)
  }

  def updateBookmarks(
                     instanceRef: NexusInstanceReference,
                     bookmarksListFromUser: List[NexusInstanceReference],
                     editorUser: EditorUser,
                     token: String
                     ): Future[List[Either[APIEditorError, Unit]]] = {
    queryService.getInstancesWithId(
      wSClient,
      config.kgQueryEndpoint,
      instanceRef,
      EditorBookmarkService.kgQueryGetInstanceBookmarksAndBookmarkList(instanceRef.nexusPath)
    ).flatMap {
      res =>
        res.status match {
          case OK =>
            (res.json \ "bookmarks").asOpt[List[JsValue]] match {
              case Some(ids) => //Delete the bookmarks
                val bookmarksFromDb = ids.filter(js =>  (js \ EditorConstants.USERID).as[List[String]].contains(editorUser.nexusUser.id))
                  .map(js => NexusInstanceReference.fromUrl( (js \ "bookmarkListId").as[List[String]].head))
                val (bookmarksToAdd, bookmarksListWithBookmarksToDelete) = BookmarkHelper.bookmarksToAddAndDelete(bookmarksFromDb, bookmarksListFromUser)
                val bookmarksToDelete = ids
                  .filter( js => bookmarksListWithBookmarksToDelete.map(_.toString).contains( (js \ "bookmarkListId").as[List[String]].head ))
                  .map( js => NexusInstanceReference.fromUrl((js \ "id").as[String]))
                val results = for {
                  added <- addInstanceToBookmarkLists(instanceRef, bookmarksToAdd, token).map[List[Either[APIEditorError, Unit]]] {
                   _.map {
                        case Right(_) => Right(())
                        case Left(error) => Left(error)
                      }
                  }
                  deleted <- removeInstanceFromBookmarkLists(instanceRef, bookmarksToDelete, token)
                } yield added ::: deleted
                results.map{
                  l => l.map{
                    case Left(error) => logger.error(s"Error while updating bookmarks - ${error.msg}")
                      Left(error)
                    case Right(()) => Right(())

                  }
                }
              case None => Future(List(Left(APIEditorError(res.status, res.body))))
            }
          case _ =>
            logger.error(s"Could not fetch bookmarks - ${res.body}")
            Future(List(Left(APIEditorError(res.status, res.body))))
        }
    }
  }



  def removeInstanceFromBookmarkLists(
                                  instanceRef: NexusInstanceReference,
                                  bookmarkListIds: List[NexusInstanceReference],
                                  token: String
                                ):
  Future[List[Either[APIEditorError, Unit]]] = {
    // Get the ids of the bookmarks
    val queries = bookmarkListIds.map{ id =>
      instanceApiService.delete(
        wSClient,
        config.kgQueryEndpoint,
        id,
        token
      ).map{
        case Left(response) => Left(APIEditorError(response.status, response.body))
        case Right(s) => Right(s)
      }
    }
    Future.sequence(queries)
  }

  def retrieveBookmarkList(instanceIds: List[NexusInstanceReference], editorUser: EditorUser):
  Future[List[(NexusInstanceReference, Either[APIEditorError, List[BookmarkList]])]] = {
    Future.sequence(instanceIds.map { ids =>
      retrieveBookmarkListSingleInstance(ids, editorUser)
    })
  }

  private def retrieveBookmarkListSingleInstance(instanceReference: NexusInstanceReference, editorUser: EditorUser):
  Future[(NexusInstanceReference ,Either[APIEditorError, List[BookmarkList]])] = {
    queryService.getInstancesWithId(
      wSClient,
      config.kgQueryEndpoint,
      instanceReference,
      EditorBookmarkService.kgQueryGetInstanceBookmarkLists(instanceReference.nexusPath)
    ).map {
      res =>
        res.status match {
          case OK =>
            (res.json \ "bookmarkList").asOpt[List[JsValue]] match {
              case Some(result) =>
                val userList = result.filter(js => (js \ EditorConstants.USERID).as[List[String]].contains(editorUser.nexusUser.id))
                  .map(_.as[BookmarkList])
                (instanceReference,  Right(userList))
              case None => (instanceReference, Left(APIEditorError(NOT_FOUND, "No result found")))
            }
          case _ => (instanceReference, Left(APIEditorError(res.status, res.body)))
        }
    }
  }

}

object EditorBookmarkService {


  def kgQueryGetUserFoldersQuery(userPath: NexusPath, context: String = EditorConstants.context): String = s"""
     |{
     |  "@context": $context,
     |  "schema:name": "",
     |  "root_schema": "nexus_instance:${userPath.toString()}",
     |  "fields": [
     |    {
     |        "fieldname": "userFolders",
     |        "relative_path": {
     |            "@id": "hbpkg:${EditorConstants.USER}",
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
     |             "relative_path": "base:${EditorConstants.RELATIVEURL}",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "folderType",
     |             "relative_path": "hbpkg:${EditorConstants.FOLDERTYPE}",
     |             "required": true
     |           },
     |           {
     |             "fieldname": "lists",
     |             "relative_path": {
     |                 "@id": "hbpkg:${EditorConstants.BOOKMARKLISTFOLDER}",
     |                 "reverse":true
     |               },
     |               "fields":[
     |               		{
     |               			"fieldname":"id",
     |               			"relative_path": "base:${EditorConstants.RELATIVEURL}"
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

  def kgQueryGetInstanceBookmarksAndBookmarkList(
                                                  instancePath: NexusPath,
                                                  context: String = EditorConstants.context
                                                ): String =
    s"""
    |{
    |  "@context": $context,
    |  "schema:name": "",
    |  "root_schema": "nexus_instance:${instancePath.toString()}",
    |  "fields": [
    |    {
    |      "fieldname": "bookmarks",
    |      "relative_path": {
    |        "@id": "hbpkg:${EditorConstants.BOOKMARKINSTANCELINK}",
    |        "reverse": true
    |      },
    |      "fields": [
    |        {
    |          "fieldname": "id",
    |          "relative_path": "base:${EditorConstants.RELATIVEURL}"
    |        },
    |        {
    |          "fieldname": "bookmarkListId",
    |          "required":true,
    |          "relative_path": [
    |            "hbpkg:${EditorConstants.BOOKMARKLIST}",
    |            "base:${EditorConstants.RELATIVEURL}"
    |          ]
    |        },
    |        {
    |          "fieldname": "${EditorConstants.USERID}",
    |          "required":true,
    |          "relative_path": [
    |            "hbpkg:${EditorConstants.BOOKMARKLIST}",
    |            "hbpkg:${EditorConstants.BOOKMARKLISTFOLDER}",
    |            "hbpkg:${EditorConstants.USER}",
    |            "hbpkg:${EditorConstants.USERID}"
    |          ]
    |        }
    |      ]
    |    }
    |  ]
    |}
    """.stripMargin

  def kgQueryGetInstanceBookmarkLists(instancePath: NexusPath,  context: String = EditorConstants.context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${instancePath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname": "bookmarkList",
       |      "relative_path": [
       |        {
       |          "@id": "hbpkg:${EditorConstants.BOOKMARKINSTANCELINK}",
       |          "reverse": true
       |        },
       |        "hbpkg:${EditorConstants.BOOKMARKLIST}"
       |      ],
       |      "fields": [
       |        {
       |          "fieldname": "name",
       |          "relative_path": "schema:name"
       |        },
       |        {
       |          "fieldname": "id",
       |          "relative_path": "base:${EditorConstants.RELATIVEURL}"
       |        },
       |        {
       |          "fieldname": "${EditorConstants.USERID}",
       |          "relative_path": [
       |            "hbpkg:${EditorConstants.BOOKMARKLISTFOLDER}",
       |            "hbpkg:${EditorConstants.USER}",
       |            "hbpkg:${EditorConstants.USERID}"
       |          ]
       |        }
       |      ]
       |    }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetInstances(bookmarkPath: NexusPath,  context: String = EditorConstants.context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${bookmarkPath.toString()}",
       |  "fields": [
       |    {
       |      "fieldname":"bookmarkListId",
       |      "relative_path":{
       |        "@id":"hbpkg:${EditorConstants.BOOKMARKLIST}"
       |      },
       |      "fields":[
       |        {
       |          "fieldname":"id",
       |          "relative_path":"base:${EditorConstants.RELATIVEURL}"
     |          }
       |       ]
       |    },
       |    {
       |               			"fieldname":"instances",
       |               			"relative_path": {
       |               				"@id":"hbpkg:${EditorConstants.BOOKMARKINSTANCELINK}"
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
       |               					"relative_path":"base:${EditorConstants.RELATIVEURL}"
       |               				}
       |               				]
       |
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarkListByIdQuery(bookmarkListPath: NexusPath,  context: String = EditorConstants.context): String =
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
       |       "relative_path":"base:${EditorConstants.RELATIVEURL}"
       |     },
       |     {
       |       "fieldname":"userFolderId",
       |       "relative_path":"hbpkg:${EditorConstants.BOOKMARKLISTFOLDER}"
       |     }
       |  ]
       |}
    """.stripMargin

  def kgQueryGetBookmarksForDeletion(bookmarkListPath: NexusPath,  context: String = EditorConstants.context): String =
    s"""
       |{
       |  "@context": $context,
       |  "schema:name": "",
       |  "root_schema": "nexus_instance:${bookmarkListPath.toString()}",
       |  "fields": [
       |  {
       |       "fieldname":"originalId",
       |       "relative_path": "base:${EditorConstants.RELATIVEURL}"
       |       },
       |    {
       |        "fieldname": "bookmarks",
       |        "relative_path": {
       |            "@id": "hbpkg:${EditorConstants.BOOKMARKLIST}",
       |            "reverse":true
       |        },
       |        "fields":[
       |               		{
       |               			"fieldname":"originalId",
       |               			"relative_path": "base:${EditorConstants.RELATIVEURL}"
       |               		}
       |              ]
       |     }
       |  ]
       |}
     """.stripMargin

  def bookmarkToNexusStruct(instanceLink: String, userBookMarkListNexusId: String): JsObject = {
    Json.obj(
      EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKLIST -> Json.obj("@id" -> s"$userBookMarkListNexusId"),
      EditorConstants.EDITORNAMESPACE +  EditorConstants.BOOKMARKINSTANCELINK -> Json.obj("@id" -> s"$instanceLink")
    )
  }

  def bookmarkListToNexusStruct(name:String, userFolderId: String): JsObject = {
    Json.obj(
      SchemaFieldsConstants.NAME -> name,
      EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKLISTFOLDER -> Json.obj("@id" -> s"$userFolderId")
    )
  }

  def bookmarkListFolderToNexusStruct(name:String, userNexusId: String, folderType: FolderType): JsObject = {
    Json.obj(
      SchemaFieldsConstants.NAME -> name,
      EditorConstants.EDITORNAMESPACE + EditorConstants.USER -> Json.obj("@id" -> s"$userNexusId"),
      EditorConstants.EDITORNAMESPACE + EditorConstants.FOLDERTYPE -> folderType.t
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
