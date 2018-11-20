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
package constants

import models.NexusPath

object EditorConstants {
  val EDITORNAMESPACE = "https://schema.hbp.eu/hbpkg/"
  val INFERENCESPACE = "https://schema.hbp.eu/inference/"
  val BASENAMESPACE = "https://schema.hbp.eu/"
  val BOOKMARKLIST = "bookmarkList"
  val BOOKMARKINSTANCELINK = "bookmarkInstanceLink"
  val BOOKMARKLISTFOLDER = "bookmarkListFolder"
  val USER = "user"
  val USERID = "userId"
  val FOLDERTYPE = "folderType"
  val ALTERNATIVES = "alternatives"


  val RELATIVEURL = "relativeUrl"

  val commonNodeTypes = List(
    "minds/core/dataset/v1.0.0",
    "minds/core/subject/v1.0.0",
    "minds/core/person/v1.0.0",
    "minds/core/placomponent/v1.0.0",
    "minds/core/specimengroup/v1.0.0",
    "minds/experiment/sample/v1.0.0",
    "minds/experiment/subject/v1.0.0"
  )
  val bookmarkListFolderPath = NexusPath("hbpkg", "core", "bookmarklistfolder", "v0.0.1")
  val bookmarkListPath = NexusPath("hbpkg", "core", "bookmarklist", "v0.0.1")
  val bookmarkPath = NexusPath("hbpkg", "core", "bookmark", "v0.0.1")



  val editorUserPath = NexusPath("hbpkg", "core", "user", "v0.0.1")



  val context =
    s"""
       |{
       |    "@vocab": "https://schema.hbp.eu/graphQuery/",
       |    "schema": "http://schema.org/",
       |    "hbpkg": "${EDITORNAMESPACE}",
       |    "base":"${BASENAMESPACE}",
       |    "nexus": "https://nexus-dev.humanbrainproject.org/vocabs/nexus/core/terms/v0.1.0/",
       |    "nexus_instance": "https://nexus-dev.humanbrainproject.org/v0/schemas/",
       |    "this": "https://schema.hbp.eu/editor/",
       |    "searchui": "https://schema.hbp.eu/search_ui/",
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
}
