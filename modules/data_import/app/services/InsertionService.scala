package data_import.services

import com.google.inject.Inject
import models.excel_import.Entity
import play.api.Configuration
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.libs.ws.WSClient
import nexus.services.NexusService

import scala.concurrent.{ExecutionContext, Future}

class InsertionService @Inject()(wSClient: WSClient, nexusService: NexusService,config: Configuration)
                                (implicit executionContext: ExecutionContext) {

  /*
  * Right (url) or Left (error message)
  */
  def insertEntity(nexusUrl:String, entity: Entity, linksRef: collection.mutable.Map[String, String],
                   token: String, nexusService: NexusService): Future[Either[(String, JsValue), String]] = {
    // insert entity
    val payload = entity.toKGPayload(linksRef)
    val identifier = (payload \ "http://schema.org/identifier").as[String]
    nexusService.insertOrUpdateInstance(nexusUrl, "uniminds", "core", entity.`type`, "v0.0.1", payload, identifier, token).flatMap{
      case (operation, idOpt, responseOpt) =>
        responseOpt match {
          case Some(response) =>
            response.map { resp =>
              resp.status match {
                case 200 | 201 =>
                  Left((operation, resp.json))
                case _ =>
                  Right(s"[${entity.`type`}] ${entity.id} --> ERROR: ${resp.bodyAsBytes.utf8String}")
              }
            }
          case None => // corresponds to ignore operation. Manually build JsValue to forward needed info like id
            Future.successful(Left((operation, JsObject(Map("@id" -> JsString(idOpt.getOrElse("")))))))
        }
    }
  }

}
