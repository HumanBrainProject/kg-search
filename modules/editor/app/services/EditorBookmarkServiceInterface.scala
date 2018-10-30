
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

import common.models.{NexusPath, PreviewInstance}
import editor.models.{APIEditorError, EditorUser, FormRegistry}
import editor.models.EditorUserList.{BOOKMARKFOLDER, BookmarkList, BookmarkListFolder, FolderType}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait EditorBookmarkServiceInterface {

  def getUserLists(editorUser: EditorUser, formRegistry: FormRegistry): Future[Either[WSResponse, List[BookmarkListFolder]]]

  def getInstanceOfBookmarkList(bookmarkListId: String, start:Int, size:Int, search:String):Future[Either[WSResponse, List[PreviewInstance]]]

  def createBookmarkListFolder(editorUser: EditorUser, name: String, folderType: FolderType = BOOKMARKFOLDER, token: String): Future[Option[BookmarkListFolder]]

  def createBookmarkList(bookmarkListName: String, folderId: String, token: String): Future[Either[WSResponse, BookmarkList]]

  def updateBookmarkList(bookmarkList: BookmarkList, userFolderId:String, revision: Long, token: String): Future[Either[WSResponse, BookmarkList]]

  def deleteBookmarkList(bookmarkListPath: NexusPath, instanceId: String, token: String): Future[Either[APIEditorError, Unit]]

  def addInstanceToBookmarkLists(instanceFullId: String, bookmarkListIds: List[String], token:String):Future[List[WSResponse]]

  def removeInstanceFromBookmarkLists(instancePath: NexusPath, instanceId: String, bookmarkListIds: List[String], token: String):Future[List[WSResponse]]

  def retrieveBookmarkList(instanceIds: List[(NexusPath, String)]):Future[Map[String, Either[APIEditorError, List[BookmarkList]]]]
}
