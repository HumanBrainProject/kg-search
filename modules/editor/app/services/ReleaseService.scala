
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
import models.{NexusPath, ReleaseStatus}
import play.api.Logger
import play.api.http.ContentTypes._
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class ReleaseService @Inject()(
                                ws: WSClient,
                                nexusService: NexusService,
                                config: ConfigurationService
                              )(implicit executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  def releaseInstance(nexusPath: NexusPath, id:String, token: String)
  :Future[Either[Option[WSResponse],  collection.Map[String, JsValue]]] = {
    val reconciledPath = nexusPath.reconciledPath(config.reconciledPrefix)
    nexusService.getInstance(s"${config.nexusEndpoint}/v0/data/${reconciledPath.toString()}/$id", token).flatMap{
      res => res.status match {
        case NOT_FOUND => this.getReleaseInstance(nexusPath, id)
        case _ => this.getReleaseInstance(reconciledPath, id)
      }
    }

  }

  private def getReleaseInstance(nexusPath: NexusPath, id: String): Future[Either[Option[WSResponse],  collection.Map[String, JsValue]]] = {
    ws.url(s"${config.kgQueryEndpoint}/arango/release/${nexusPath.toString()}/$id").addHttpHeaders(CONTENT_TYPE -> JSON).get().map{
      res => res.status match{
        case OK =>
          val item = res.json.as[JsObject]
          if(item != null){
            val spec = ReleaseService.recSpec(item)
            Right(spec.as[JsObject].value)
          }else{
            Left(None)
          }
        case _ => logger.error(res.body)
          Left(Some(res))
      }
    }
  }

  // TOFIX change the graph api to something more appropriate
  def releaseStatus(nexusPath: NexusPath, id:String):Future[Either[(String, WSResponse), JsObject]] = {

    ws.url(s"${config.kgQueryEndpoint}/arango/releasestatus/${nexusPath.toString()}/$id").addHttpHeaders(CONTENT_TYPE -> JSON).get().map{
      res => res.status match{
        case OK =>
          val item = res.json.as[JsObject]
          val spec = ReleaseService.reduceChildrenStatus(item, s"${nexusPath.toString()}/$id")
          Right(spec)
        case _ => logger.error(res.body)
          Left((s"${nexusPath.toString()}/$id",res))
      }
    }
  }
}

object ReleaseService {
  def recSpec(js: JsValue): JsValue = {
    js match {
      case js: JsObject =>
        Json.toJson(
          js.as[JsObject].value.map {
            case (k, v) => k match {
              case "http://schema.org/name" => "label" -> v
              case "@type" => "type" -> JsString(v.as[String].split("#").last)
              case "children" => k -> recSpec(v)
              case "status" =>
                val arr = v.as[List[String]]
                val status = if (arr.isEmpty) {
                  "NOT_RELEASED"
                } else {
                  if (arr.contains("released")) {
                    "RELEASED"
                  } else {
                    "NOT_RELEASED"
                  }
                }
                k -> JsString(status)
              case _ => k -> v
            }
          }
        )
      case js: JsArray =>
        Json.toJson(
          js.as[List[JsValue]]
            .filter(j => j != null && j != JsNull)
            .map(recSpec)
            .groupBy(js => (js \ "@id").as[String])
            .map {
              case (k, v) =>
                val edgeTypes = v.foldLeft(List[String]()) {
                  case (list, j) => (j \ "linkType").as[String].split("/").head.split("-").last :: list
                }
                val transformer = (__ \ 'linkType).json.put(Json.toJson(edgeTypes))
                val linkType = v.head.transform(transformer)
                k -> v.head.as[JsObject].++(linkType.get)
            }
            .values.toList
            .sortBy(js => ((js \ "type").asOpt[String].getOrElse(""), (js \ "@id").asOpt[String].getOrElse("")) )
        )
      case el => el
    }
  }

  def getWorstChildrenStatus(item: JsObject):String = {
    val childrenStatus = (item \ "child_status").as[List[String]]
    if(childrenStatus.isEmpty){
      ""
    } else if(childrenStatus.contains(ReleaseStatus.notReleased)){
      ReleaseStatus.notReleased
    }else if(childrenStatus.contains(ReleaseStatus.hasChanged)){
      ReleaseStatus.hasChanged
    }else if(childrenStatus.contains(ReleaseStatus.released)){
      ReleaseStatus.released
    }else{
      ""
    }
  }

  def reduceChildrenStatus(item:JsObject, originalId: String): JsObject = {
    val childrenStatus = getWorstChildrenStatus(item)
    item +("childrenStatus" -> JsString(childrenStatus)) - "child_status" + ("id" -> JsString(originalId))
  }
}
