package constants

import models.NexusPath

object EditorConstants {
  val EDITORNAMESPACE = "http://hbp.eu/kgeditor/"
  val BOOKMARKLIST = s"${EDITORNAMESPACE}bookmarkList"
  val BOOKMARKINSTANCELINK = s"${EDITORNAMESPACE}bookmarkInstanceLink"
  val BOOKMARKLISTFOLDER = s"${EDITORNAMESPACE}bookmarkListFolder"
  val USER = s"${EDITORNAMESPACE}user"
  val FOLDERTYPE = s"${EDITORNAMESPACE}folderType"

  val IDRESPONSEFIELD= "relativeUrl"

  val commonNodeTypes = List("minds/core/dataset/v0.0.4")
  val bookmarkListFolderPath = NexusPath("kg", "core", "bookmarklistfolder", "v0.0.1")
  val bookmarkListPath = NexusPath("kg", "core", "bookmarklist", "v0.0.1")
  val bookmarkPath = NexusPath("kg", "core", "bookmark", "v0.0.1")
}
