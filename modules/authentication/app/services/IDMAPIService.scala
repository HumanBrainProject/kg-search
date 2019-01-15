package services

import com.google.inject.Inject
import models.user.IDMUser
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class IDMAPIService @Inject()(WSClient: WSClient, config: ConfigurationService)(
  implicit executionContext: ExecutionContext
) {

  def getUserInfoFromID(userId: String, token: String): Future[Option[IDMUser]] = {
    val url = s"${config.idmApiEndpoint}/user/$userId"
    WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token).get().map { res =>
      res.status match {
        case OK => Some(res.json.as[IDMUser])
        case _  => None
      }
    }
  }

}
