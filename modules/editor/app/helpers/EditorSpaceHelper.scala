
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

package helpers

import models.user.{NexusUser, OIDCUser}


object EditorSpaceHelper {
  private val editorSuffix = "editor"

  def nexusEditorContext(org: String): String = {
    if(org != "manual"){
      s""""${org}":"http://hbp.eu/${org}#",
         |"manual":"http://hbp.eu/manual#",
       """.stripMargin
    } else {
      s""""${org}":"http://hbp.eu/${org}#","""
    }
  }



  /**
    * Check if the group is an editor group
    * @param userInfo UserInfo with all the groups
    * @param hintedGroup The group the user wants to use
    * @return true if the group is an editor group
    */
  def isEditorGroup(userInfo: NexusUser, hintedGroup: String): Boolean = {
    userInfo.organizations.contains(s"${hintedGroup}")
  }

  def getGroupName(group:String, suffix:String) : String = {
    s"$group$suffix"
  }
}
