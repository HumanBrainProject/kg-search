
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

package editor.services

import com.google.inject.Inject
import common.models.NexusPath
import nexus.services.NexusService
import play.api.{Configuration, Logger}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._
import play.api.http.HeaderNames._
import play.api.http.ContentTypes._

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Results._
import common.services.ConfigurationService

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
            val spec = ReleaseService.specs(item)
            Right(spec)
          }else{
            Left(None)
          }
        case _ => logger.error(res.body)
          Left(Some(res))
      }
    }
  }

  // TOFIX change the graph api to something more appropriate
  def releaseStatus(org: String, domain: String, schema: String, version: String, id:String):Future[Either[(String, WSResponse), JsObject]] = {

    ws.url(s"${config.kgQueryEndpoint}/arango/releasestatus/$org/$domain/$schema/$version/$id").addHttpHeaders(CONTENT_TYPE -> JSON).get().map{
      res => res.status match{
        case OK =>
          val item = res.json.as[JsObject]
          val spec = ReleaseService.reduceChildrenStatus(item, s"${NexusPath(org, domain, schema,version).toString()}/$id")
          Right(spec)
        case _ => logger.error(res.body)
          Left((s"$org/$domain/$schema/$version/$id",res))
      }
    }
  }
}

object ReleaseService {

  def specs(item: JsObject): collection.Map[String, JsValue] = {
    item.value.map{ k =>
      k._1 match {
        case "http://schema.org/name" => "label" -> k._2
        case "@type" => "type" -> JsString(k._2.as[String].split("#").last)
        case "children" => k._1 -> Json.toJson(k._2.as[JsArray]
          .value.groupBy(js => (js \ "@id").as[String]).map{
          case (k,v) =>
            val edgeTypes = v.foldLeft(List[String]()) {
              case (list, js) => (js \ "linkType").as[String].split("/").head.split("-").last :: list
            }
            val transformer = (__ \ 'linkType).json.put(Json.toJson(edgeTypes))
            val linkType = v.head.transform(transformer)
            k -> v.head.as[JsObject].++(linkType.get)
        }.values.map( j => specs(j.as[JsObject]))).as[JsValue]
        case "linkType" =>
          k._1 -> k._2
        case "status" =>
          val arr = k._2.as[List[String]]
          val status = if(arr.isEmpty){
            "NOT_RELEASED"
          }else{
            if(arr.contains("released")){
              "RELEASED"
            }else{
              "NOT_RELEASED"
            }
          }
          k._1 -> JsString(status)
        case _ => k._1 -> k._2
      }
    }
  }

  def getWorstChildrenStatus(item: JsObject):String = {
    val childrenStatus = (item \ "child_status").as[List[String]]
    if(childrenStatus.isEmpty){
      ""
    } else if(childrenStatus.contains("NOT_RELEASED")){
      "NOT_RELEASED"
    }else if(childrenStatus.contains("HAS_CHANGED")){
      "HAS_CHANGED"
    }else {
      "RELEASED"
    }
  }

  def reduceChildrenStatus(item:JsObject, originalId: String): JsObject = {
    val childrenStatus = getWorstChildrenStatus(item)
    item +("childrenStatus" -> JsString(childrenStatus)) - "child_status" + ("id" -> JsString(originalId))
  }
}
