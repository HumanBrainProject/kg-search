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

import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.NexusPath
import editor.helpers.FormHelper
import nexus.services.NexusService
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._
import play.api.http.HeaderNames._
import common.services.ConfigurationService
import editor.helper.InstanceHelper

import scala.concurrent.{ExecutionContext, Future}

class ArangoQueryService @Inject()(
                                    config: ConfigurationService,
                                    wSClient: WSClient,
                                    nexusService: NexusService,
                                    oIDCAuthService: OIDCAuthService
                                  )(implicit executionContext: ExecutionContext) {

  def graphEntities(org: String,
                    domain: String,
                    schema: String,
                    version: String,
                    id: String,
                    step: Int,
                    token: String
                   ): Future[Either[WSResponse, JsObject]] = {
    val path = NexusPath(org, domain, schema, version)
    val reconciledPath = path.reconciledPath(config.reconciledPrefix)
    nexusService.getInstance(s"${config.nexusEndpoint}/v0/data/${reconciledPath.toString()}/$id", token).flatMap{
      res => res.status match {
        case NOT_FOUND => this.graph(path, id, step)
        case OK => this.graph(reconciledPath, id, step)
        case _ => Future{Left(res)}
      }
    }

  }

  def listInstances(nexusPath: NexusPath, from: Option[Int], size: Option[Int], search: String): Future[Either[WSResponse, JsObject]] = {
    wSClient.url(s"${config.kgQueryEndpoint}/arango/instances/${nexusPath.toString()}")
      .withQueryStringParameters(("search", search), ("from", from.getOrElse("").toString), ("size", size.getOrElse("").toString)).get().map{
      res =>
        res.status match {
          case OK =>
            val total = if((res.json \ "fullCount").as[Long] == 0){
              (res.json \ "count").as[Long]
            }else{
              (res.json \ "fullCount").as[Long]
            }
            val data = (res.json \ "data").as[JsArray]
            Right(
              Json.obj("data" -> InstanceHelper.formatInstanceList( data, config.reconciledPrefix),
                "label" -> JsString(
                  (FormHelper.formRegistry \ nexusPath.org \ nexusPath.domain \ nexusPath.schema \ nexusPath.version \ "label").asOpt[String]
                    .getOrElse(nexusPath.toString())
                ),
                "dataType" -> (data.value.head \ "@type").as[JsString],
                "total" -> total
              )
            )
          case _ => Left(res)
        }
    }
  }

  private def graph(nexusPath: NexusPath, id:String, step:Int): Future[Either[WSResponse, JsObject]] = {
    wSClient
      .url(s"${config.kgQueryEndpoint}/arango/graph/${nexusPath.toString}/$id?step=$step")
      .addHttpHeaders(CONTENT_TYPE -> "application/json").get().map {
      allRelations =>
        allRelations.status match {
          case OK =>

            val j = allRelations.json.as[List[JsObject]]
            val edges: List[JsObject] = j.flatMap { el =>
              ArangoQueryService.formatEdges((el \ "edges").as[List[JsObject]])
            }.distinct
            val vertices = j.flatMap { el =>
              ArangoQueryService.formatVertices((el \ "vertices").as[List[JsValue]])
            }.distinct
            Right(Json.obj("links" -> edges, "nodes" -> vertices))
          case _ => Left(allRelations)
        }
    }
  }
}

object ArangoQueryService {
  val camelCase = """(?=[A-Z])"""

  def idFormat(id: String): String = {
    val l = id.split("/")
    val path = l.head.replaceAll("-", "/").replaceAll("_", ".")
    val i = l.last
    s"$path/$i"
  }

  def formatVertices(vertices: List[JsValue]): List[JsObject] = {
    vertices
      .map {
        case v: JsObject =>
          (v \ "@type").asOpt[String].map { d =>
            val dataType = d.split("#").last.capitalize
            val label = dataType.split(camelCase).mkString(" ")
            val title = if ((v \ "http://schema.org/name").asOpt[JsString].isDefined) {
              (v \ "http://schema.org/name").as[JsString]
            } else {
              Json.toJson(v)
            }
            Json.obj(
              "id" -> Json.toJson(ArangoQueryService.idFormat((v \ "_id").as[String])),
              "name" -> JsString(label),
              "dataType" -> JsString(d),
              "title" -> title
            )
          }.getOrElse(JsNull)
        case _ => JsNull
      }
      .filter(v => v != JsNull && isInSpec( (v \ "id").as[String].splitAt((v \ "id").as[String].lastIndexOf("/"))._1))
      .map(_.as[JsObject])

  }

  def formatEdges(edges: List[JsObject]): List[JsObject] = {
    edges.map { j =>
      val id = (j \ "_id").as[String]
      val titleRegex = id.split("/").head
      val title = titleRegex
        .splitAt(titleRegex.lastIndexOf("-"))
        ._2.substring(1)
        .replaceAll("_", " ")
        .split(camelCase)
        .mkString(" ")
        .capitalize
      Json.obj(
        "source" -> ArangoQueryService.idFormat((j \ "_from").as[String]),
        "target" -> ArangoQueryService.idFormat((j \ "_to").as[String]),
        "id" -> id,
        "title" -> title
      )
    }
  }

  def isInSpec(id:String):Boolean = {
    val list = (FormHelper.editableEntitiyTypes \ "data")
      .as[List[JsObject]]
      .map(js => (js  \ "path").as[String])
    list.contains(id)
  }
}
