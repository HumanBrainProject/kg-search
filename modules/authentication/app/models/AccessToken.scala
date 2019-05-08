package models

sealed trait AccessToken {
  val token: String
}

case class BasicAccessToken(override val token: String) extends AccessToken
case class RefreshAccessToken(override val token: String) extends AccessToken
