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
import constants.QueryConstants._
import constants.{EditorConstants, SchemaFieldsConstants}
import helpers.BookmarkHelper
import models._
import models.editorUserList._
import models.errors.APIEditorError
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, PreviewInstance}
import models.specification.{FormRegistry, QuerySpec, UISpec}
import models.user.{EditorUser, NexusUser}
import monix.eval.Task
import play.api.Logger
import play.api.http.ContentTypes._
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import services._
import services.instance.InstanceApiService
import services.query.{QueryApiParameter, QueryService}
import services.specification.{FormOp, FormService}

class EditorBookmarkService @Inject()(
  config: ConfigurationService,
  wSClient: WSClient,
  nexusService: NexusService,
  nexusExtensionService: NexusExtensionService
)(implicit OIDCAuthService: OIDCAuthService, clientCredentials: CredentialsService)
    extends EditorBookmarkServiceInterface {
  val logger = Logger(this.getClass)

  object instanceApiService extends InstanceApiService
  object queryService extends QueryService

  def getUserBookmarkLists(
    editorUser: EditorUser,
    formRegistry: FormRegistry[UISpec],
    token: AccessToken
  ): Task[Either[APIEditorError, List[BookmarkListFolder]]] = {
    queryService
      .getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        editorUser.nexusId,
        QuerySpec(
          Json.parse(EditorBookmarkService.kgQueryGetUserFoldersQuery(EditorConstants.editorUserPath)).as[JsObject]
        ),
        token,
        QueryApiParameter()
      )
      .map { res =>
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
  private def getEditableEntities(
    nexusUser: NexusUser,
    formRegistry: FormRegistry[UISpec]
  ): List[BookmarkListFolder] = {
    val allEditableEntities = FormOp
      .editableEntities(nexusUser, formRegistry)
      .foldLeft(SystemDefinedFolder.getFolders) {
        case (acc, (path, uiSpec)) =>
          val userList = BookmarkList(
            path.toString(),
            uiSpec.label,
            uiSpec.isEditable,
            uiSpec.uiInfo,
            uiSpec.color
          )
          uiSpec.folderID match {
            case Some(id) =>
              acc.get(id) match {
                case Some(folder) =>
                  acc.updated(id, folder.copy(userLists = userList :: folder.userLists))
                case None =>
                  val f = BookmarkListFolder(
                    None,
                    uiSpec.folderName.getOrElse(id),
                    NODETYPEFOLDER,
                    List(userList)
                  )
                  acc.updated(id, f)
              }
            case None => SystemDefinedFolder.addToDefaultFolder(acc, userList)
          }
      }
    allEditableEntities.values.toList.sortBy(_.folderName).map(s => s.copy(userLists = s.userLists.sortBy(_.name)))
  }

  def getInstancesOfBookmarkList(
    bookmarkListId: NexusInstanceReference,
    start: Int,
    size: Int,
    search: String,
    token: AccessToken
  ): Task[Either[APIEditorError, (List[PreviewInstance], Long)]] = {
    val q = wSClient
      .url(s"${config.kgQueryEndpoint}/arango/bookmarks/${bookmarkListId.toString}")
      .withQueryStringParameters(START -> start.toString, SIZE -> size.toString, SEARCH -> search)
      .withHttpHeaders(CONTENT_TYPE -> JSON, AUTHORIZATION -> token.token)
    val r = token match {
      case BasicAccessToken(_)   => Task.deferFuture(q.get())
      case RefreshAccessToken(_) => AuthHttpClient.getWithRetry(q)
    }

    r.map { res =>
      res.status match {
        case OK =>
          val instances = (res.json \ "data").as[List[PreviewInstance]]
          val total = (res.json \ "count").as[Long]
          Right((instances, total))
        case _ => Left(APIEditorError(res.status, res.body))
      }
    }
  }

  def createBookmarkListFolder(
    user: EditorUser,
    name: String,
    token: AccessToken,
    folderType: FolderType = BOOKMARKFOLDER
  ): Task[Either[APIEditorError, BookmarkListFolder]] = {
    val payload = EditorBookmarkService.bookmarkListFolderToNexusStruct(
      name,
      s"${config.nexusEndpoint}/v0/data/${user.nexusId}",
      folderType
    )
    instanceApiService
      .post(
        wSClient,
        config.kgQueryEndpoint,
        NexusInstance(None, EditorConstants.bookmarkListFolderPath, payload),
        None,
        token
      )
      .map {
        case Right(ref) =>
          Right(BookmarkListFolder(Some(ref), name, folderType, List()))
        case Left(res) =>
          logger.error("Error while creating a user folder " + res.body)
          Left(APIEditorError(res.status, res.body))
      }
  }

  def createBookmarkList(
    bookmarkListName: String,
    folderId: String,
    token: AccessToken
  ): Task[Either[APIEditorError, BookmarkList]] = {
    val payload =
      EditorBookmarkService.bookmarkListToNexusStruct(bookmarkListName, s"${config.nexusEndpoint}/v0/data/$folderId")
    instanceApiService
      .post(
        wSClient,
        config.kgQueryEndpoint,
        NexusInstance(None, EditorConstants.bookmarkListPath, payload),
        None,
        token
      )
      .map {
        case Right(ref) =>
          Right(BookmarkList(ref.toString, bookmarkListName, None, None, None))
        case Left(res) =>
          logger.error("Error while creating a bookmark list " + res.body)
          Left(APIEditorError(res.status, res.body))
      }
  }

  def getBookmarkListById(
    instanceReference: NexusInstanceReference,
    token: AccessToken
  ): Task[Either[APIEditorError, (BookmarkList, String)]] = {
    queryService
      .getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        instanceReference,
        QuerySpec(
          Json.parse(EditorBookmarkService.kgQueryGetBookmarkListByIdQuery(instanceReference.nexusPath)).as[JsObject]
        ),
        token,
        QueryApiParameter()
      )
      .map { res =>
        res.status match {
          case OK =>
            val id = NexusInstanceReference.fromUrl((res.json \ "id").as[String])
            val name = (res.json \ "name").as[String]
            val bookmarkList = BookmarkList(id.toString(), name, None, None, None)
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
    bookmarkListRef: NexusInstanceReference,
    userFolderId: String,
    newDate: Option[String],
    userId: String,
    token: AccessToken
  ): Task[Either[APIEditorError, BookmarkList]] = {
    instanceApiService
      .put(
        wSClient,
        config.kgQueryEndpoint,
        bookmarkListRef,
        EditorInstance(
          NexusInstance(
            Some(bookmarkListRef.id),
            bookmarkListRef.nexusPath,
            EditorBookmarkService.bookmarkListToNexusStruct(bookmarkList.name, userFolderId, newDate)
          )
        ),
        token,
        userId
      )
      .map {
        case Right(()) => Right(bookmarkList)
        case Left(err) => Left(APIEditorError(err.status, err.body))
      }
  }

  def deleteBookmarkList(
    bookmarkRef: NexusInstanceReference,
    token: AccessToken
  ): Task[Either[APIEditorError, Unit]] = {
    queryService
      .getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        bookmarkRef,
        QuerySpec(
          Json.parse(EditorBookmarkService.kgQueryGetBookmarksForDeletion(bookmarkRef.nexusPath)).as[JsObject]
        ),
        token,
        QueryApiParameter()
      )
      .flatMap { res =>
        res.status match {
          case OK =>
            val bookmarksToDelete = (res.json \ "bookmarks").as[List[JsObject]].map { js =>
              NexusInstanceReference.fromUrl((js \ "originalId").as[String])
            }
            // Delete all bookmarks
            val listOfFuture = for {
              ref <- bookmarksToDelete
            } yield {
              instanceApiService.delete(wSClient, config.kgQueryEndpoint, ref, token)
            }
            Task.gather(listOfFuture).flatMap[Either[APIEditorError, Unit]] { listOfResponse =>
              if (listOfResponse.forall(_.isRight)) {
                logger.debug("All the bookmarks are deleted. We can safely delete the bookmark list")
                // Delete the bookmark list
                instanceApiService
                  .delete(
                    wSClient,
                    config.kgQueryEndpoint,
                    bookmarkRef,
                    token
                  )
                  .map {
                    case Right(()) => Right(())
                    case Left(r)   => Left(APIEditorError(r.status, r.body))
                  }
              } else {
                logger.error("Could not delete all the bookmarks")
                val compiledMessage = listOfResponse
                  .filterNot(_.isRight)
                  .map {
                    case Left(response) => s"${response.status} - ${response.statusText} - ${response.body}"
                    case _              => ""
                  }
                  .mkString("\n")
                Task.pure(Left(APIEditorError(INTERNAL_SERVER_ERROR, compiledMessage)))
              }
            }
          case _ =>
            logger.error(s"Could not fetch the bookmarks to be deleted ${res.body}")
            Task.pure(Left(APIEditorError(INTERNAL_SERVER_ERROR, "Could not fetch data to delete ")))
        }
      }
  }

  def addInstanceToBookmarkLists(
    instanceReference: NexusInstanceReference,
    bookmarkListIds: List[NexusInstanceReference],
    token: AccessToken
  ): Task[List[Either[APIEditorError, Unit]]] = {
    val queries = bookmarkListIds.map { ref =>
      val toInsert = EditorBookmarkService
        .bookmarkToNexusStruct(
          s"${config.nexusEndpoint}/v0/data/${instanceReference.toString}",
          s"${config.nexusEndpoint}/v0/data/${ref.toString}"
        )
      instanceApiService
        .post(
          wSClient,
          config.kgQueryEndpoint,
          NexusInstance(None, EditorConstants.bookmarkPath, toInsert),
          None,
          token
        )
        .map {
          case Left(res) =>
            logger.error(s"Error while adding bookmarks - ${res.body}")
            Left(APIEditorError(res.status, res.body))
          case Right(_) => Right(())
        }
    }
    Task.gather(queries)
  }

  def updateBookmarks(
    instanceRef: NexusInstanceReference,
    bookmarksListFromUser: List[NexusInstanceReference],
    editorUser: EditorUser,
    token: AccessToken
  ): Task[List[Either[APIEditorError, Unit]]] = {
    queryService
      .getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        instanceRef,
        QuerySpec(
          Json
            .parse(EditorBookmarkService.kgQueryGetInstanceBookmarksAndBookmarkList(instanceRef.nexusPath))
            .as[JsObject]
        ),
        token,
        QueryApiParameter()
      )
      .flatMap { res =>
        res.status match {
          case OK =>
            (res.json \ "bookmarks").asOpt[List[JsValue]] match {
              case Some(ids) => //Delete the bookmarks
                val bookmarksFromDb = ids
                  .filter(js => (js \ EditorConstants.USERID).as[List[String]].contains(editorUser.nexusUser.id))
                  .map(js => NexusInstanceReference.fromUrl((js \ "bookmarkListId").as[List[String]].head))
                val (bookmarksToAdd, bookmarksListWithBookmarksToDelete) =
                  BookmarkHelper.bookmarksToAddAndDelete(bookmarksFromDb, bookmarksListFromUser)
                val bookmarksToDelete = ids
                  .filter(
                    js =>
                      bookmarksListWithBookmarksToDelete
                        .map(_.toString)
                        .contains((js \ "bookmarkListId").as[List[String]].head)
                  )
                  .map(js => NexusInstanceReference.fromUrl((js \ "id").as[String]))
                for {
                  added   <- addInstanceToBookmarkLists(instanceRef, bookmarksToAdd, token)
                  deleted <- removeInstanceFromBookmarkLists(instanceRef, bookmarksToDelete, token)
                } yield added ::: deleted
              case None =>
                for {
                  added <- addInstanceToBookmarkLists(instanceRef, bookmarksListFromUser, token)
                } yield added
            }
          case _ =>
            logger.error(s"Could not fetch bookmarks - ${res.body}")
            Task.pure(List(Left(APIEditorError(res.status, res.body))))
        }
      }
  }

  def removeInstanceFromBookmarkLists(
    instanceRef: NexusInstanceReference,
    bookmarkIds: List[NexusInstanceReference],
    token: AccessToken
  ): Task[List[Either[APIEditorError, Unit]]] = {
    // Get the ids of the bookmarks
    val queries = bookmarkIds.map { id =>
      instanceApiService
        .delete(
          wSClient,
          config.kgQueryEndpoint,
          id,
          token
        )
        .map {
          case Left(response) => Left(APIEditorError(response.status, response.body))
          case Right(s)       => Right(s)
        }
    }
    Task.gather(queries)
  }

  def retrieveBookmarkLists(
    instanceIds: List[NexusInstanceReference],
    editorUser: EditorUser,
    token: AccessToken
  ): Task[List[(NexusInstanceReference, Either[APIEditorError, List[BookmarkList]])]] = {
    Task.gather(instanceIds.map { ids =>
      retrieveBookmarkListSingleInstance(ids, editorUser, token)
    })
  }

  private def retrieveBookmarkListSingleInstance(
    instanceReference: NexusInstanceReference,
    editorUser: EditorUser,
    token: AccessToken
  ): Task[(NexusInstanceReference, Either[APIEditorError, List[BookmarkList]])] = {
    queryService
      .getInstancesWithId(
        wSClient,
        config.kgQueryEndpoint,
        instanceReference,
        QuerySpec(
          Json.parse(EditorBookmarkService.kgQueryGetInstanceBookmarkLists(instanceReference.nexusPath)).as[JsObject]
        ),
        token,
        QueryApiParameter()
      )
      .map { res =>
        res.status match {
          case OK =>
            (res.json \ "bookmarkList").asOpt[List[JsValue]] match {
              case Some(result) =>
                val userList = result
                  .filter(js => (js \ EditorConstants.USERID).as[List[String]].contains(editorUser.nexusUser.id))
                  .map(_.as[BookmarkList])
                (instanceReference, Right(userList))
              case None => (instanceReference, Right(List()))
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

  def kgQueryGetInstanceBookmarkLists(instancePath: NexusPath, context: String = EditorConstants.context): String =
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

  def kgQueryGetInstances(bookmarkPath: NexusPath, context: String = EditorConstants.context): String =
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

  def kgQueryGetBookmarkListByIdQuery(bookmarkListPath: NexusPath, context: String = EditorConstants.context): String =
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

  def kgQueryGetBookmarksForDeletion(bookmarkListPath: NexusPath, context: String = EditorConstants.context): String =
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
      EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKLIST         -> Json.obj("@id" -> s"$userBookMarkListNexusId"),
      EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKINSTANCELINK -> Json.obj("@id" -> s"$instanceLink")
    )
  }

  def bookmarkListToNexusStruct(name: String, userFolderId: String, newDate: Option[String] = None): JsObject =
    newDate match  {
      case Some(d) => Json.obj(
        SchemaFieldsConstants.NAME                                           -> name,
        EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKLISTFOLDER -> Json.obj("@id" -> s"$userFolderId"),
        SchemaFieldsConstants.lastUpdate -> d
      )
      case None => Json.obj(
        SchemaFieldsConstants.NAME                                           -> name,
        EditorConstants.EDITORNAMESPACE + EditorConstants.BOOKMARKLISTFOLDER -> Json.obj("@id" -> s"$userFolderId")
      )
    }

  def bookmarkListFolderToNexusStruct(name: String, userNexusId: String, folderType: FolderType): JsObject = {
    Json.obj(
      SchemaFieldsConstants.NAME                                   -> name,
      EditorConstants.EDITORNAMESPACE + EditorConstants.USER       -> Json.obj("@id" -> s"$userNexusId"),
      EditorConstants.EDITORNAMESPACE + EditorConstants.FOLDERTYPE -> folderType.t
    )
  }

  implicit object JsEither {
    implicit def eitherReads[A, B](implicit A: Reads[A], B: Reads[B]): Reads[Either[A, B]] =
      Reads[Either[A, B]] { json =>
        A.reads(json) match {
          case JsSuccess(value, path) => JsSuccess(Left(value), path)
          case JsError(e1) =>
            B.reads(json) match {
              case JsSuccess(value, path) => JsSuccess(Right(value), path)
              case JsError(e2)            => JsError(JsError.merge(e1, e2))
            }
        }
      }

    implicit def eitherWrites[A, B](implicit A: Writes[A], B: Writes[B]): Writes[Either[A, B]] =
      Writes[Either[A, B]] {
        case Left(a)  => A.writes(a)
        case Right(b) => B.writes(b)
      }

    implicit def eitherFormat[A, B](implicit A: Format[A], B: Format[B]): Format[Either[A, B]] =
      Format(eitherReads, eitherWrites)
  }
}
