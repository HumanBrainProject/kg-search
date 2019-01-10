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
package models

import models.editorUserList.{BOOKMARKFOLDER, BookmarkList, BookmarkListFolder}
import models.instance.NexusInstanceReference
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsString, Json}

class BookmarkListFolderSpec extends PlaySpec {

  "toJson" should {
    "contain the correct fields" in {
      val folderId = NexusInstanceReference("org", "domain", "schema", "version", "folderId")
      val uFolder = BookmarkListFolder(
        Some(folderId),
        "name",
        BOOKMARKFOLDER,
        List(
          BookmarkList(
            "id",
            "myList",
            None,
            None,
            None
          ),
          BookmarkList(
            "id2",
            "my 2nd List",
            None,
            None,
            None
          )
        )
      )

      val expected = Json.obj(
        "id"         -> JsString(folderId.toString),
        "folderName" -> JsString("name"),
        "folderType" -> JsString("BOOKMARK"),
        "lists" -> Json.arr(
          Json.obj(
            "id"   -> "id",
            "name" -> "myList"
          ),
          Json.obj(
            "id"   -> "id2",
            "name" -> "my 2nd List"
          )
        )
      )

      Json.toJson(uFolder) mustBe expected
    }
  }

}
