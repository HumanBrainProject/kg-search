
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

import models.errors.APIEditorError
import models._
import models.editorUserList.{BOOKMARKFOLDER, BookmarkList, BookmarkListFolder, FolderType}
import models.instance.{NexusInstanceReference, PreviewInstance}
import models.user.EditorUser
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

trait EditorBookmarkServiceInterface {

  def getUserLists(editorUser: EditorUser, formRegistry: FormRegistry):
  Future[Either[APIEditorError, List[BookmarkListFolder]]]

  def getInstancesOfBookmarkList(bookmarkListId: NexusInstanceReference, start: Int, size: Int, search: String):
  Future[Either[APIEditorError, (List[PreviewInstance], Long)]]

  def createBookmarkListFolder(editorUser: EditorUser, name: String, folderType: FolderType = BOOKMARKFOLDER, token: String):
  Future[Either[APIEditorError, BookmarkListFolder]]

  def createBookmarkList(bookmarkListName: String, folderId: String, token: String):
  Future[Either[APIEditorError, BookmarkList]]

  def updateBookmarkList(
                          bookmarkList: BookmarkList,
                          bookmarkListPath: NexusPath,
                          bookmarkListId: String,
                          userFolderId: String,
                          token: String):
  Future[Either[APIEditorError, BookmarkList]]

  def deleteBookmarkList(bookmarkRef: NexusInstanceReference, token: String):
  Future[Either[APIEditorError, Unit]]

  def addInstanceToBookmarkLists(
                                  instanceReference: NexusInstanceReference,
                                  bookmarkListIds: List[NexusInstanceReference],
                                  token: String
                                ):
  Future[List[Either[APIEditorError, NexusInstanceReference]]]

  def removeInstanceFromBookmarkLists(
                                       instanceRef: NexusInstanceReference,
                                       bookmarkListIds: List[NexusInstanceReference],
                                       token: String
                                     ):
  Future[List[Either[APIEditorError, Unit]]]

  def retrieveBookmarkList(instanceIds: List[NexusInstanceReference], editorUser: EditorUser):
  Future[List[(NexusInstanceReference, Either[APIEditorError, List[BookmarkList]])]]
}
