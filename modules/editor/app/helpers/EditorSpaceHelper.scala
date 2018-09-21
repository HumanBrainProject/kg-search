
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

import common.models.OIDCUser


object EditorSpaceHelper {
  private val editorSuffix = "-editor"
  private val nexusPrefix = "nexus-"
  private val pattern = s"""$nexusPrefix(\\w+)$editorSuffix""".r

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
    * From the user info return the list of groups that have an editor space
    * @param userInfo
    * @return
    */
  def editorGroups(userInfo: OIDCUser):List[String] = {
    userInfo.groups
      .filter( group => group.startsWith(nexusPrefix) && group.endsWith(editorSuffix))
      .map{
          case pattern(editorGroup) => Some(editorGroup)
          case _ => None
      }
      .filter(_.isDefined)
      .map(_.get)
      .toList
      .filter(group => userInfo.groups.contains(s"$nexusPrefix$group"))
  }

  /**
    * Check if the group is an editor group
    * @param userInfo UserInfo with all the groups
    * @param hintedGroup The group the user wants to use
    * @return true if the group is an editor group
    */
  def isEditorGroup(userInfo: OIDCUser, hintedGroup: String): Boolean = {
    editorGroups(userInfo).contains(hintedGroup)
  }

  def getGroupName(group:String, suffix:String) : String = {
    s"$group$suffix"
  }
}
