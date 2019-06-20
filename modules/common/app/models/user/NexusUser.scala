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

import play.api.libs.json.{JsPath, Reads, Writes}

trait NexusUserInfo {
  val organizations: Seq[String]
}

class NexusUser(
  override val id: String,
  override val name: String,
  override val email: String,
  override val picture: Option[String],
  override val groups: Seq[String],
  override val organizations: Seq[String]
) extends OIDCUser(id, name, email, picture, groups)
    with NexusUserInfo
    with Serializable {}

object NexusUser {

  def unapply(user: NexusUser): Option[(String, String, String, Option[String], Seq[String], Seq[String])] =
    Some((user.id, user.name, user.email, user.picture, user.groups, user.organizations))

  def apply(id: String, name: String, email: String, picture: Option[String], groups: Seq[String], organizations: Seq[String]): NexusUser =
    new NexusUser(
      id,
      name,
      email,
      picture,
      groups,
      organizations
    )

  import play.api.libs.functional.syntax._
  implicit val editorUserWrites: Writes[NexusUser] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String] and
    (JsPath \ "picture").writeNullable[String] and
    (JsPath \ "groups").write[Seq[String]] and
    (JsPath \ "organizations").write[Seq[String]]
  )(unlift(NexusUser.unapply))

  implicit val editorUserReads: Reads[NexusUser] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "picture").readNullable[String] and
    (JsPath \ "groups").read[Seq[String]] and
    (JsPath \ "organizations").read[Seq[String]]
  )(NexusUser.apply _)
}
