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
package nexus.services

import com.google.inject.Inject
import nexus.helpers.NexusHelper.{domainDefinition, minimalSchemaDefinition, schemaDefinitionForEditor}
import nexus.helpers.NexusHelper.hash
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import NexusService._
import common.models.{NexusPath, ReleaseInstance}
import play.api.Configuration
import common.services.ConfigurationService


class NexusService @Inject()(wSClient: WSClient, config:ConfigurationService)(implicit executionContext: ExecutionContext) {

  def releaseInstance(instanceData: JsValue, token: String): Future[JsObject] = {
    val jsonObj = instanceData.as[JsObject]
    val instanceUrl = s"${config.nexusEndpoint}/v0/data/${(jsonObj \ "id").as[String]}"
    val instanceRev = (jsonObj \ "rev").as[Long]
    // check if this instance is attached to a release instance already
    isReleased(instanceUrl, instanceRev, token).flatMap{ releasedId =>
      releasedId match {
        case Some(releaseInstance) =>
          if (releaseInstance.instanceRevision == instanceRev){
            Future.successful(JsObject(Map(
              "status" -> JsString(RELEASE_ALREADY),
              "released_id" -> JsString(releaseInstance.id())
            )))
          }else{
            updateReleaseInstance(instanceUrl, s"${config.nexusEndpoint}/v0/data/${releaseInstance.id()}", instanceRev,releaseInstance.revision, token).map{
              case (status, response) =>
                response.status match {
                  case 200 | 201 =>
                    JsObject(Map(
                      "status" -> JsString(RELEASE_OK),
                      "released_id" -> (response.json.as[JsObject] \ "@id").as[JsString]
                    ))
                  case _ =>
                    JsObject(Map(
                      "status" -> JsString(RELEASE_KO)
                    ))
                }
            }
          }
        case None =>
          createReleaseInstance(instanceUrl, instanceRev, token).map{ response =>
            response.status match {
              case 200 | 201 =>
                JsObject(Map(
                  "status" -> JsString(RELEASE_OK),
                  "released_id" -> (response.json.as[JsObject] \ "@id").as[JsString]
                ))
              case _ =>
                JsObject(Map(
                  "status" -> JsString(RELEASE_KO)
                ))
            }
          }
      }
    }
  }

  def isReleased(instanceUrl: String, instanceRev: Long, token: String): Future[Option[ReleaseInstance]] = {
    listAllNexusResult(s"${instanceUrl}/incoming?deprecated=false&fields=all", token).map{ incomingsLinks =>
        val found = incomingsLinks.find(
          result => (result.as[JsObject] \ "resultId").as[String].contains("prov/release"))
        found.map(value => ReleaseInstance((value.as[JsObject] \ "source" ).as[JsObject]))
    }
  }

  def createReleaseInstance(instanceUrl: String, instanceRev:Long, token: String): Future[WSResponse] = {
    val (Seq(id, version, schema, domain, org, apiVersion, data), baseUrlSeq) = instanceUrl.split("/").reverse.toSeq.splitAt(7)
    val baseUrl = baseUrlSeq.reverse.mkString("/")
    val releaseIdentifier = hash(instanceUrl)
    val payload = Json.parse(ReleaseInstance.template(instanceUrl, releaseIdentifier, instanceRev))
    val path = NexusPath(org, "prov", "release", "v0.0.1")
    insertInstance(baseUrl, path, payload, token)
  }

  def updateReleaseInstance(instanceUrl: String, releaseUrl: String, instanceRev:Long, releaseRev:Long, token: String): Future[(String, WSResponse)] = {
    val releaseIdentifier = hash(instanceUrl)
    val payload = Json.parse(ReleaseInstance.template(instanceUrl, releaseIdentifier, instanceRev))
    updateInstance(releaseUrl, Some(releaseRev), payload, token)
  }

  /**
    * Create a schema and publish it if it does not exists
    * @param nexusUrl The base url of the nexus instance
    * @param destinationOrg the organization where the schema should be created
    * @param nexusPath The path of the entity
    * @param editorOrg is used for extended information on the entity such as the origin, the updater id (is usually the same as org)
    * @param token the access token
    * @return
    */
  def createSchema(nexusUrl:String, destinationOrg: String,  nexusPath: NexusPath, editorOrg: String, token: String, editorContext:String = ""): Future[WSResponse] = {

    val schemaUrl = s"${nexusUrl}/v0/schemas/${destinationOrg}/${nexusPath.domain}/${nexusPath.schema.toLowerCase}/${nexusPath.version}"
    wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response => response.status match {
        case 200 => // schema exists already
          Future.successful(response)
        case 404 => // schema not found, create it
          val newSchemaDef = if(editorOrg != nexusPath.org){
            schemaDefinitionForEditor.replace("${editorContext}", editorContext)
          }else {
            schemaDefinitionForEditor.replace("${editorContext}", "")
          }
          val schemaContent = Json.parse(newSchemaDef.replace("${entityType}", nexusPath.schema)
            .replace("${org}", nexusPath.org).replace("${editorOrg}", editorOrg).replaceAll("\r\n", ""))
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


  def createSimpleSchema(nexusUrl:String, nexusPath: NexusPath, token: String, namespaceOpt: Option[String] = None): Future[WSResponse] = {
    val nameSpace = namespaceOpt.getOrElse(s"http://hbp.eu/${nexusPath.org}").replaceAll("#$", "") // use org by default
    val schemaUrl = s"${nexusUrl}/v0/schemas/${nexusPath.org}/${nexusPath.domain}/${nexusPath.schema.toLowerCase}/${nexusPath.version}"
    wSClient.url(schemaUrl).addHttpHeaders("Authorization" -> token).get().flatMap{
      response =>
        response.status match {
        case 200 => // schema exists already
          Future.successful(response)
        case 404 => // schema not found, create it
          val schemaContent = Json.parse(minimalSchemaDefinition.replace("${entityType}", nexusPath.schema)
            .replace("${nameSpace}", nameSpace).replaceAll("\r\n", ""))
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


  def retrieveInstanceById(nexusUrl:String,nexusPath: NexusPath,
                           identifier: String, token: String): Future[WSResponse] = {
    val instanceUrl = s"${nexusUrl}/v0/data/${nexusPath.org}/${nexusPath.domain}/${nexusPath.schema.toLowerCase}/${nexusPath.version}"
    val filterQuery = s"""filter={"path":"http://schema.org/identifier","op":"eq","value":"$identifier"}&fields=all&deprecated=false"""
    wSClient.url(s"${instanceUrl}?$filterQuery")
      .addHttpHeaders("Authorization" -> token)
      .get()
  }

  def getInstance(instanceUrl: String, token: String): Future[WSResponse] = {
    wSClient.url(instanceUrl).addHttpHeaders("Authorization" -> token).get()
  }

  def insertOrUpdateInstance(nexusUrl:String, nexusPath: NexusPath,
                             payload: JsValue, identifier: String, token: String): Future[(String, Option[String], Option[Future[WSResponse]])] = {
    retrieveInstanceById(nexusUrl, nexusPath, identifier, token).flatMap {
      response =>
        response.status match {
          case 200 => // analyze response
            val total = (response.json \ "total").as[Int]
            total match {
              case 1 => // update
                val id = (response.json \ "results" \ 0 \ "resultId").as[String]
                val revision = (response.json \ "results" \ 0 \ "source" \ "nxv:rev").as[Long]
                updateInstance(id, Some(revision), payload, token).map {
                  case (operation, response) =>
                    (operation, Some(id), Some(Future.successful(response)))
                }
              case _ => // insert if not or ambiguous
                insertInstance(nexusUrl, nexusPath, payload, token).map{ insertResp =>
                  val id = (insertResp.json.as[JsObject] \ "@id").asOpt[String]
                  (INSERT, id, Some(Future.successful(insertResp)))
                }
            }
          case _ => // error looking for instance, forward ERROR
            Future.successful(ERROR, None, Some(Future.successful(response)))

        }
    }
  }

  def insertInstance(nexusUrl:String, nexusPath: NexusPath, payload: JsValue, token: String): Future[WSResponse] = {
    val instanceUrl = s"${nexusUrl}/v0/data/${nexusPath.org}/${nexusPath.domain}/${nexusPath.schema.toLowerCase}/${nexusPath.version}"
    val payloadWihtHash = payload.as[JsObject].+("http://hbp.eu/internal#hashcode", JsString(hash(payload.toString())))
    wSClient.url(instanceUrl).addHttpHeaders("Authorization" -> token).post(payloadWihtHash).flatMap{
      response => response.status match {
        case 200 | 201 => // instance inserted
          Future.successful(response)

        case _ => // forward error message from nexus
          Future.successful(response)
      }
    }
  }

  def updateInstanceLastRev(instanceUrl: String, payload: JsValue, token: String): Future[(String, WSResponse)] = {
    getInstance(instanceUrl, token).flatMap{
      response =>
        try {
          val rev = (response.json \ "nxv:rev").as[Long]
          updateInstance(instanceUrl, Some(rev), payload, token)
        } catch {
          case _: Throwable =>
            Future.successful(UPDATE, response)
        }
      }
  }

  def updateInstance(instanceUrl: String, revOpt: Option[Long], payload: JsValue, token: String): Future[(String, WSResponse)] = {
    revOpt match {
      case Some(rev) =>
        getInstance(instanceUrl, token).flatMap {
          case response =>
            val prevHashCode = (response.json \ "http://hbp.eu/internal#hashcode").asOpt[String].getOrElse("")
            val curHashCode = (payload \ "http://hbp.eu/internal#hashcode").asOpt[String].getOrElse(hash(payload.toString()))
            if (prevHashCode != curHashCode) {
              val payloadWithHash = payload.as[JsObject].+("http://hbp.eu/internal#hashcode", JsString(curHashCode))
              wSClient.url(instanceUrl)
                .addQueryStringParameters(("rev", rev.toString) )
                .addHttpHeaders("Authorization" -> token)
                .put(payloadWithHash).map{
                  updateRes => updateRes.status match {
                  case 200 | 201 => // instance updated
                    (UPDATE, updateRes)
                  case _ => // forward error message from nexus
                    (SKIP, updateRes)
                }
              }
            } else {
              Future.successful(SKIP, response)
            }
          }
      case None =>
        updateInstanceLastRev(instanceUrl, payload, token)
    }
  }

  def listAllNexusResult(url: String, token: String): Future[Seq[JsValue]] = {
    val sizeLimit = 50
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

  def deprecateInstance(nexusEndpoint: String, nexusPath: NexusPath, id: String, token: String): Future[WSResponse] = {
    val instanceUrl = s"${nexusEndpoint}/v0/data/${nexusPath.org}/${nexusPath.domain}/${nexusPath.schema.toLowerCase}/${nexusPath.version}/${id}"
    wSClient.url(instanceUrl).withHttpHeaders("Authorization" -> token).delete()
  }
}

object NexusService {
  val UPDATE = "UPDATE"
  val INSERT = "INSERT"
  val SKIP = "SKIP"
  val ERROR = "ERROR"

  val RELEASE_OK = "RELEASED WITH SUCCESS"
  val RELEASE_KO = "ERROR IN RELEASING INSTANCE"
  val RELEASE_ALREADY = "INSTANCE ALREADY RELEASED"
}
