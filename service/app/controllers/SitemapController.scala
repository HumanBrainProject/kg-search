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

import javax.inject.Inject
import models.errors.ApiError
import models.templates.TemplateType
import monix.eval.Task
import monix.execution.Scheduler
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logging}
import play.cache.NamedCache
import services.indexer.{ElasticSearch, Indexer}

import scala.xml.Elem

class SitemapController @Inject()(
  elasticSearch: ElasticSearch,
  indexer: Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]],
  configuration: Configuration,
  cc: ControllerComponents,
  @NamedCache("search-metadata-cache") cache: AsyncCacheApi
) extends AbstractController(cc) with Logging {
  implicit val s: Scheduler = monix.execution.Scheduler.Implicits.global
  val searchUrl: String = configuration.get[String]("searchUrlBase")

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
            val root = <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">{listOfXmlElements.distinct}</urlset>
            Ok(root).as("text/xml")
          }
        }
      case Left(e) => Task.pure(e.toResults())
    }
    siteMapGeneration.runToFuture
  }

  private def generateXmlPerType(templateType: TemplateType): Task[List[Either[ApiError, Elem]]] = {
    Task.deferFuture(cache.get[List[Either[ApiError,Elem]]](s"xml-${templateType.apiName}")).flatMap {
      case Some(elem) =>
        logger.info(s"Found sitemap xml for ${templateType.apiName} in cache")
        Task.pure(elem)
      case _ =>
        elasticSearch.queryIndexByType(templateType).map {
          case Right(listOfElements) =>
            listOfElements
              .map { el =>
                (el \ "_source" \ "identifier" \ "value")
                  .asOpt[String]
                  .map { id =>
                    val location = s"$searchUrl/instances/${templateType.apiName}/$id"
                    Right(<url><loc>{location}</loc></url>)
                  }
                  .getOrElse(Left(ApiError(INTERNAL_SERVER_ERROR, s"Could not parse identifier of element $el")))
              }
          case Left(e) => List(Left(e))
      }
    }
  }
}
