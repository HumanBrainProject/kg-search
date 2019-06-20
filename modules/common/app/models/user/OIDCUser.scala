
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

package models.user

import play.api.libs.json.{JsPath, Reads}

/**
  * The user information gathered from the OIDC API
  * @param id
  * @param name
  * @param email
  * @param picture
  * @param groups
  */
class OIDCUser(val id: String,val name:String, val email:String, val picture:Option[String], val groups:Seq[String]) extends User with Serializable {

}
object OIDCUser {

  val idLabel = "sub"
  val nameLabel = "name"
  val emailLabel = "email"
  val pictureLabel = "picture"
  val groupsLabel = "groups"
  val mandatoryFields = Seq(idLabel, nameLabel, emailLabel, pictureLabel, groupsLabel)

  import play.api.libs.functional.syntax._

  implicit val readerOidUser : Reads[OIDCUser] = (
    (JsPath \ idLabel).read[String] and
      (JsPath \ nameLabel).read[String] and
      (JsPath \ emailLabel).read[String] and
      (JsPath \ pictureLabel).readNullable[String] and
      (JsPath \ groupsLabel).read[String].map(_.split(",").toSeq)
    ) (new OIDCUser(_, _, _, _,_ ) )


  def unapply(arg: OIDCUser): Option[(String, String, String, Option[String], Seq[String])] = Some((arg.id, arg.name, arg.email, arg.picture, arg.groups))


}
