package services

import common.helpers.ESHelper
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ESService @Inject()(wSClient: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) {
  val esHost: String = configuration.get[String]("es.host")
  def getEsIndices(): Future[List[String]] = {
    wSClient.url(esHost + s"/${ESHelper.indicesPath}?format=json").get().map { res =>
      val j = res.json
      j.as[List[JsValue]].map(json =>
        (json \ "index").as[String]
      )
    }
  }

}
