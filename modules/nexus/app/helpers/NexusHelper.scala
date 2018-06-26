
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

package nexus.helpers

import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

object NexusHelper {

  val schemaDefinition = """
    {
      "@type": "owl:Ontology",
      "@context": {
          "datatype": {
              "@id": "sh:datatype",
              "@type": "@id"
          },
          "name": "sh:name",
          "path": {
              "@id": "sh:path",
              "@type": "@id"
          },
          "property": {
              "@id": "sh:property",
              "@type": "@id"
          },
          "targetClass": {
              "@id": "sh:targetClass",
              "@type": "@id"
          },
          "${org}": "http://hbp.eu/${org}#",
          "schema": "http://schema.org/",
          "sh": "http://www.w3.org/ns/shacl#",
          "owl": "http://www.w3.org/2002/07/owl#",
          "xsd": "http://www.w3.org/2001/XMLSchema#",
          "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
          "shapes": {
              "@reverse": "rdfs:isDefinedBy",
              "@type": "@id"
          }
      },
      "shapes": [
        {
          "@id": "${org}:${entityType}Shape",
          "@type": "sh:NodeShape",
          "property": [
            {
              "datatype": "xsd:string",
              "path": "${org}:origin"
            }
          ],
          "targetClass": "${org}:${entityType}"
        }
      ]
    }
    """

  def domainDefinition(description: String): JsObject = {
    Json.obj(
      "description" -> description
    )
  }

  def createSchema(nexusUrl:String, org: String, entityType: String, space: String, version: String, token: String)(implicit ws: WSClient, ec: ExecutionContext): Future[WSResponse] = {

    val schemaUrl = s"${nexusUrl}/v0/schemas/${space}/${entityType.toLowerCase}/${version}"
    ws.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case 200 => // schema exists already
          Future.successful(response)
        case 404 => // schema not found, create it
          val schemaContent = Json.parse(schemaDefinition.replace("${entityType}", entityType).replace("${org}", org))
          ws.url(schemaUrl).addHttpHeaders("Authorization" -> token).put(schemaContent).flatMap{
            schemaCreationResponse => schemaCreationResponse.status match {
              case 201 => // schema created, publish it
                ws.url(s"$schemaUrl/config?rev=1").addHttpHeaders("Authorization" -> token).patch(
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

  def createDomain(nexusUrl:String, org: String, domain: String, domainDescription: String, token: String)(implicit ws: WSClient, ec: ExecutionContext): Future[WSResponse] = {
    assert(domain.forall(_.isLetterOrDigit))
    val schemaUrl = s"${nexusUrl}/v0/domains/$org/${domain.toLowerCase}/"
    ws.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case OK => // schema exists already
          Future.successful(response)
        case NOT_FOUND => // schema not found, create it
          val payload = domainDefinition(domainDescription)
          ws.url(schemaUrl).addHttpHeaders("Authorization" -> token).put(payload).flatMap{
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


  def listAllNexusResult(url: String, token: String)(implicit ws: WSClient, ec: ExecutionContext): Future[Seq[JsValue]] = {
    val sizeLimit = 5
    val initialUrl = (url.contains("?size="), url.contains("&size=")) match {
      case (true, _) => url
      case (_ , true) => url
      case (false, false) => if(url.contains("?")) s"$url&size=$sizeLimit" else s"$url?size=$sizeLimit"
    }

    ws.url(initialUrl).addHttpHeaders("Authorization" -> token).get().flatMap {
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
                        ws.url(nextUrl).addHttpHeaders("Authorization" -> token).get().map { response =>
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
