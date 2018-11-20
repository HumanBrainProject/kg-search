package services

import com.google.inject.Inject
import models.NexusPath
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.ws.{EmptyBody, WSClient, WSResponse}
import play.api.http.Status._
import play.api.http.ContentTypes._

import scala.concurrent.{ExecutionContext, Future}

class NexusExtensionService @Inject() (config: ConfigurationService,
                                       wSClient: WSClient,
                                      )(implicit executionContext: ExecutionContext){
  val schemaPath ="/api/schemas"

  def createSimpleSchema(nexusPath: NexusPath, subSpace: Option[String] ): Future[Either[WSResponse, Unit]] = {
    wSClient.url(s"${config.kgQueryEndpoint}$schemaPath/${nexusPath.toString()}")
      .withQueryStringParameters( "subSpace" -> subSpace.getOrElse(""))
      .withHttpHeaders(CONTENT_TYPE -> JSON)
      .put(EmptyBody)
      .map{ res =>
        res.status match {
          case OK => Right(())
          case _ => Left(res)
        }
    }
  }

}
