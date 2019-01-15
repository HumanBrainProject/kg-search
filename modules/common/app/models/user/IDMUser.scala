package models.user

import models.user.IDMUser.ID
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._
case class IDMUser(
  id: ID,
  userName: String,
  givenName: String,
  familyName: String,
  displayName: String,
  emails: List[Email]
) {}

object IDMUser {
  type ID = String
  implicit val idmUserReads: Reads[IDMUser] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "username").read[String] and
    (JsPath \ "givenName").read[String] and
    (JsPath \ "familyName").read[String] and
    (JsPath \ "displayName").read[String] and
    (JsPath \ "emails").read[List[Email]]
  )(IDMUser.apply _)
}

case class Email(value: String, primary: Boolean, verified: Boolean)

object Email {
  implicit val idmEmailReads: Reads[Email] = (
    (JsPath \ "value").read[String] and
    (JsPath \ "primary").read[Boolean] and
    (JsPath \ "verified").read[Boolean]
  )(Email.apply _)
}
