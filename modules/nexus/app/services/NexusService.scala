
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

package services

import com.google.inject.Inject
import nexus.helpers.NexusHelper.{domainDefinition, schemaDefinition}
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsArray, JsBoolean, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class NexusService @Inject()(wSClient: WSClient)(implicit executionContext: ExecutionContext) {

  /**
    * Create a schema and publish it if it does not exists
    * @param nexusUrl The base url of the nexus instance
    * @param destinationOrg the organization where the schema should be created
    * @param org the organisation oof the entity
    * @param entityType the datatype of the entity
    * @param domain the domain of the entity
    * @param version the version of the entity
    * @param editorOrg is used for extended information on the entity such as the origin, the updater id (is usually the same as org)
    * @param token the access token
    * @return
    */
  def createSchema(nexusUrl:String, destinationOrg: String,  org: String, entityType: String, domain: String, version: String, editorOrg: String, token: String, editorContext:String = ""): Future[WSResponse] = {

    val schemaUrl = s"${nexusUrl}/v0/schemas/${destinationOrg}/${domain}/${entityType.toLowerCase}/${version}"
    wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case 200 => // schema exists already
          Future.successful(response)
        case 404 => // schema not found, create it
          val newSchemaDef = if(editorOrg != org){
            schemaDefinition.replace("${editorContext}", editorContext)
          }else {
            schemaDefinition.replace("${editorContext}", "")
          }
          val schemaContent = Json.parse(newSchemaDef.replace("${entityType}", entityType)
            .replace("${org}", org).replace("${editorOrg}", editorOrg).replaceAll("\r\n", ""))
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
