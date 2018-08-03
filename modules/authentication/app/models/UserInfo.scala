
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

package authentication.models

import play.api.libs.json.JsObject

/**
  * The user information gathered from the OIDC API
  * @param id
  * @param name
  * @param email
  * @param groups
  */
case class UserInfo(id: String, name:String, email:String, groups:Seq[String])
object UserInfo {

  val idLabel = "sub"
  val nameLabel = "name"
  val emailLabel = "email"
  val groupsLabel = "groups"
  val mandatoryFields = Seq(idLabel, nameLabel, emailLabel, groupsLabel)

  def apply(json: JsObject) = {
    new UserInfo(
      id = (json \ idLabel).asOpt[String].getOrElse(""),
      name = (json \ nameLabel).asOpt[String].getOrElse(""),
      email = (json \ emailLabel).asOpt[String].getOrElse(""),
      groups = (json \ groupsLabel).asOpt[String].getOrElse("").split(",").toSeq
    )
  }


}
