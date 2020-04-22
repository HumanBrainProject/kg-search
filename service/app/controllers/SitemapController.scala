/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
package controllers

import java.nio.file.{Files, Paths}

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File
import javax.inject.Inject
import models.errors.ApiError
import models.templates.TemplateType
import monix.eval.Task
import monix.execution.Scheduler
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.indexer.{ElasticSearch, Indexer}

import scala.util.{Failure, Success, Try}
import scala.xml.Elem

class SitemapController @Inject()(
  elasticSearch: ElasticSearch,
  indexer: Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]],
  configuration: Configuration,
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val s: Scheduler = monix.execution.Scheduler.Implicits.global
  val baseUrl: String = configuration.get[String]("serviceUrlBase")

  def generateSitemap(): Action[AnyContent] = Action.async {
    val siteMapGeneration = indexer.getRelevantTypes().flatMap {
      case Right(relevantTypes) =>
        val taskOfMAyXml = Task.sequence(relevantTypes.map { t =>
          generateXmlPerType(t)
        })
        taskOfMAyXml.map { e =>
          val l = e.flatten
          val errors = l.collect { case Left(e) => e }
          if (errors.nonEmpty) {
            ApiError(INTERNAL_SERVER_ERROR, errors.map(e => e.message).mkString("\n")).toResults()
          } else {
            val listOfXmlElements = l.collect { case Right(xml) => xml }
            val root = <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">{listOfXmlElements}</urlset>
            Ok(root).as("text/xml")
          }
        }
      case Left(e) => Task.pure(e.toResults())
    }
    siteMapGeneration.runToFuture
  }

  private def generateXmlPerType(templateType: TemplateType): Task[List[Either[ApiError, Elem]]] = {
    elasticSearch.queryIndexByType(templateType).map {
      case Right(listOfElements) =>
        listOfElements.map { el =>
          (el \ "_source" \ "identifier" \ "value")
            .asOpt[String]
            .map { id =>
              val location = s"$baseUrl/search/?search=false&identifier=${templateType.apiName}/$id"
              Right(<url>
                <loc>
                  {location}
                </loc>
              </url>)
            }
            .getOrElse(Left(ApiError(INTERNAL_SERVER_ERROR, s"Could not parse identifier of element $el")))
        }
      case Left(e) => List(Left(e))
    }
  }
}
