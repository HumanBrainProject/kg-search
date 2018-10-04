
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

package common.models

import play.api.libs.json.{JsObject, JsPath, Reads}

/**
  * The user information gathered from the OIDC API
  * @param id
  * @param name
  * @param email
  * @param groups
  */
class OIDCUser(val id: String,val name:String, val email:String, val groups:Seq[String]) extends User with Serializable {

}
object OIDCUser {

  val idLabel = "sub"
  val nameLabel = "name"
  val emailLabel = "email"
  val groupsLabel = "groups"
  val mandatoryFields = Seq(idLabel, nameLabel, emailLabel, groupsLabel)

  implicit val readerOidUser=  Reads[EditorUser] = (
      (JsPath \ idLabel).read[String],
     (JsPath \ nameLabel).read[String],
     (JsPath \ emailLabel).read[String],
    (JsPath \ groupsLabel).read[String].map(_.split(",").toSeq)
    )(OIDCUser.apply _)


  def unapply(arg: OIDCUser): Option[(String, String, String, Seq[String])] = Some((arg.id, arg.name, arg.email, arg.groups))


}
