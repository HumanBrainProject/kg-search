package services

import com.google.inject.Inject
import nexus.helpers.NexusHelper.{domainDefinition, schemaDefinition}
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsArray, JsBoolean, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class NexusService @Inject()(wSClient: WSClient)(implicit executionContext: ExecutionContext) {

  def createSchema(nexusUrl:String, org: String, entityType: String, space: String, version: String, token: String): Future[WSResponse] = {

    val schemaUrl = s"${nexusUrl}/v0/schemas/${space}/${entityType.toLowerCase}/${version}"
    wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case 200 => // schema exists already
          Future.successful(response)
        case 404 => // schema not found, create it
          val schemaContent = Json.parse(schemaDefinition.replace("${entityType}", entityType).replace("${org}", org))
          wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).put(schemaContent).flatMap{
            schemaCreationResponse => schemaCreationResponse.status match {
              case 201 => // schema created, publish it
                wSClient.url(s"$schemaUrl/config?rev=1").addHttpHeaders("Authorization" -> token).patch(
                  Json.obj("published" -> JsBoolean(true))
                )
              case _ =>
                Future.successful(response)
            }
          }
        case _ =>
          Future.successful(response)
      }
    }
  }

  def createDomain(nexusUrl:String, org: String, domain: String, domainDescription: String, token: String): Future[WSResponse] = {
    assert(domain.forall(_.isLetterOrDigit))
    val schemaUrl = s"${nexusUrl}/v0/domains/$org/${domain.toLowerCase}/"
    wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case OK => // schema exists already
          Future.successful(response)
        case NOT_FOUND => // schema not found, create it
          val payload = domainDefinition(domainDescription)
          wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).put(payload).flatMap{
            domainCreationResponse => domainCreationResponse.status match {
              case _ =>
                Future.successful(response)
            }
          }
        case _ =>
          Future.successful(response)
      }
    }
  }


  def listAllNexusResult(url: String, token: String): Future[Seq[JsValue]] = {
    val sizeLimit = 5
    val initialUrl = (url.contains("?size="), url.contains("&size=")) match {
      case (true, _) => url
      case (_ , true) => url
      case (false, false) => if(url.contains("?")) s"$url&size=$sizeLimit" else s"$url?size=$sizeLimit"
    }

    wSClient.url(initialUrl).addHttpHeaders("Authorization" -> token).get().flatMap {
      response => response.status match {
        case 200 =>
          val firstResults = (response.json \ "results").as[JsArray].value
          (response.json \ "links" \ "next").asOpt[String] match {
            case Some(nextLink) =>
              // compute how many additional call will be needed
              val nbCalls = ((response.json \ "total").as[Int] / (sizeLimit.toDouble)).ceil.toInt
              Range(1, nbCalls).foldLeft(Future.successful((nextLink, firstResults))) {
                case (previousCallState, callIdx) =>
                  previousCallState.flatMap {
                    case (nextUrl, previousResult) =>
                      if (nextUrl.nonEmpty) {
                        wSClient.url(nextUrl).addHttpHeaders("Authorization" -> token).get().map { response =>
                          response.status match {
                            case 200 =>
                              val newUrl = (response.json \ "links" \ "next").asOpt[String].getOrElse("")
                              val newResults = previousResult ++ (response.json \ "results").as[JsArray].value
                              (newUrl, newResults)
                            case _ =>
                              ("", previousResult)
                          }
                        }
                      } else {
                        Future.successful(("", previousResult))
                      }
                  }
              }.map(_._2)
            case _ =>
              Future.successful(firstResults)
          }
        case _ =>
          Future.successful(Seq.empty[JsValue])
      }
    }
  }


}
