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

import models.BasicAccessToken
import models.user.IDMUser
import play.api.http.HeaderNames._
import play.api.mvc.Request

object OIDCHelper {

  /**
    * Retrieve the user's token from a request
    * @param request the request
    * @return A String containing the token or an empty String
    */
  def getTokenFromRequest[A](request: Request[A]): BasicAccessToken = {
    BasicAccessToken(request.headers.toMap.getOrElse(AUTHORIZATION, Seq("")).head)
  }

  /**
   * Check if group
   *
   * @param userInfo The user's info
   * @param group The wished group
   * @return The requested ES index or the public index
   */
  def groupNeedsPermissions(group: String): Boolean = {
    group match {
      case ESHelper.publicGroupName   => false
      case _                          => true
    }
  }

  /**
   * Check if the user is granted access to the group
   *
   * @param userInfo The user's info
   * @param group The wished group
   * @return The requested ES index or the public index
   */
  def isUserGrantedAccessToGroup(userInfo: IDMUser, group: String): Boolean = {
    val groups = ESHelper.filterNexusGroups(userInfo.groups)
    group match {
      case ESHelper.publicGroupName                                 => true
      case ESHelper.curatedGroupName if groups.contains("curated")  => true
      case group if groups.contains(group)                          => true
      case _                                                        => false
    }
  }

  /**
    * If the user is allowed, return the ES index requested by the user,
    * or else return the public index
    *
    * @param group The group
    * @param dataType The type
    * @return The requested ES index
    */
  def getESIndex(group: String, dataType: String): String = {
    group match {
      case ESHelper.publicGroupName    => s"${ESHelper.publicIndexPrefix}_${dataType.toLowerCase}"
      case ESHelper.curatedGroupName   => s"${ESHelper.curatedIndexPrefix}_${dataType.toLowerCase}"
      case group                       => s"${group}_${dataType.toLowerCase}"
    }
  }
}

