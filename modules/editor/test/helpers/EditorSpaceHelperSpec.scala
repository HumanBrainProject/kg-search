
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

package editor.helpers

import authentication.models.UserInfo
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class EditorSpaceHelperSpec  extends PlaySpec with GuiceOneAppPerSuite{

  "EditorSpaceHelper" should{
    "return the formatted list of editor groups" in {
      val originalGroups = List(
        "nexus-minds",
        "nexus-minds-editor",
        "nexus-minds-admin",
        "nexus-juelich",
        "nexus-juelich-admin",
        "hbp-somegroup",
        "nexus-group1-editor",
        "hbp-somegroup-editor",
        "nexus-group1",
        "nexus-wronglycreated-editor"
      )
      val expectedGroups = List(
        "minds",
        "group1"
      )
      val userInfo = UserInfo("123", "John Doe", "john@doe.com", originalGroups)
      val resGroups = EditorSpaceHelper.editorGroups(userInfo)
      assert(resGroups == expectedGroups)
    }
  }
}
