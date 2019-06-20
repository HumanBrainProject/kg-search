/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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

import models.user.IDMUser.ID
import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._
final case class IDMUser(
  id: ID,
  userName: String,
  givenName: String,
  familyName: String,
  displayName: String,
  emails: List[Email],
  picture: Option[String],
  isCurator: Option[Boolean]
)

object IDMUser {
  type ID = String
  implicit val idmUserReads: Reads[IDMUser] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "username").read[String] and
    (JsPath \ "givenName").read[String] and
    (JsPath \ "familyName").read[String] and
    (JsPath \ "displayName").read[String] and
    (JsPath \ "emails").read[List[Email]] and
    (JsPath \ "picture").readNullable[String] and
    (JsPath \ "isCurator").readNullable[Boolean]
  )(IDMUser.apply _)

  implicit val idmUserWrites: Writes[IDMUser] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "username").write[String] and
    (JsPath \ "givenName").write[String] and
    (JsPath \ "familyName").write[String] and
    (JsPath \ "displayName").write[String] and
    (JsPath \ "emails").write[List[Email]] and
    (JsPath \ "picture").writeNullable[String] and
    (JsPath \ "isCurator").writeNullable[Boolean]
  )(unlift(IDMUser.unapply))
}

case class Email(value: String, primary: Boolean, verified: Boolean)

object Email {
  implicit val idmEmailReads: Reads[Email] = (
    (JsPath \ "value").read[String] and
    (JsPath \ "primary").read[Boolean] and
    (JsPath \ "verified").read[Boolean]
  )(Email.apply _)

  implicit val idmEmailWrites: Writes[Email] = (
    (JsPath \ "value").write[String] and
    (JsPath \ "primary").write[Boolean] and
    (JsPath \ "verified").write[Boolean]
  )(unlift(Email.unapply))
}
