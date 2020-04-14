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
package services.indexer

import java.nio.file.Path
import java.util.{Date, UUID}

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{DatabaseScope, INFERRED, PaginationParams, RELEASED}
import models.errors.ApiError
import models.templates.{Template, TemplateType}
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.{Configuration, Logging}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.concurrent.Await

@ImplementedBy(classOf[IndexerImpl])
trait Indexer[Content, TransformedContent, Effect[_], UploadResult, QueryResult] {

  def transform(jsonContent: Content, template: Template): TransformedContent
  def transformMeta(jsonContent: Content, template: Template): TransformedContent

  def queryByType(
    templateType: TemplateType,
    databaseScope: DatabaseScope,
    paginationParams: PaginationParams,
    restrictToOrgs: List[String],
    token: String
  ): Effect[QueryResult]

  def queryByTypeAndId(
    templateType: TemplateType,
    id: UUID,
    databaseScope: DatabaseScope,
    token: String
  ): Effect[QueryResult]

  def metaByType(
    templateType: TemplateType,
    token: String,
    fieldsOnly: Boolean = true
  ): Effect[QueryResult]

  def getLabels(
    templateType: TemplateType,
    token: String
  ): Effect[QueryResult]

  def index(
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    token: String
  ): Effect[Either[ApiError, Unit]]

  def indexByType(
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    relevantType: String,
    token: String
  ): Effect[Either[ApiError, Unit]]
}

class IndexerImpl @Inject()(
  WSClient: WSClient,
  templateEngine: TemplateEngine[JsValue, JsValue],
  elasticSearch: ElasticSearch,
  siteMapGenerator: SitemapGenerator,
  configuration: Configuration
) extends Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]]
    with Logging {

  val queryEndpoint: String = configuration.get[String]("kgquery.endpoint")
  val serviceUrlBase: String = configuration.get[String]("serviceUrlBase")

  private def getRelevantTypes(token: String): Task[Either[ApiError, List[String]]] = {
    Task
      .fromFuture(
        WSClient.url(s"${queryEndpoint}/query/search/schemas").addHttpHeaders("Authorization" -> s"Bearer $token").get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            Right(
              wsresult.json
                .as[List[JsObject]]
                .map(js => js.value.get("https://schema.hbp.eu/relativeUrl").get.as[String])
            )
          case s => Left(ApiError(s, wsresult.body))
        }
      }

  }

  def createLabels(relevantTypes: List[String], token: String): Task[Map[String, JsValue]] = {
    val labelsPerRelevantTypes = relevantTypes.map { t =>
      val template = TemplateType(t.splitAt(t.lastIndexOf("/"))._2)
      metaByType(template, token, fieldsOnly = false)
        .map {
          case Right(jsValue) =>
            //TODO add specification properties
            Right((template.apiName, jsValue))
          case Left(e) => Left(e)
        }
    }
    Task.sequence(labelsPerRelevantTypes).map { l =>
      l.collect { case Right(v) => v }.foldLeft(Map[String, JsValue]("serviceUrl" -> JsString(serviceUrlBase))) {
        case (acc, (apiName, content)) =>
          acc.updated(apiName, content)
      }
    }

  }
  private def createESmapping(relevantTypes: List[String], token: String): Task[Map[String, Map[String, JsValue]]] = {
    val labels = relevantTypes.map { t =>
      val templateName = TemplateType.apply(t)
      getLabels(templateName, token).map {
        case Right(js) => Right((templateName.apiName, js))
        case Left(e)   => Left(e)
      }
    }
    Task.sequence(labels).map { listOfLabelsPerType =>
      listOfLabelsPerType
        .collect { case Right(v) => v }
        .foldLeft(Map[String, Map[String, JsValue]]("mappings" -> Map())) {
          case (acc, (apiName, js)) =>
            val innerMapUpdated = acc("mappings").updated(
              apiName,
              Json.toJson(
                Map("properties" -> js)
              )(Writes.genericMapWrites)
            )
            acc.updated("mappings", innerMapUpdated)
        }
    }
  }

  private def extractIdentifier(el: JsObject): Option[String] = {
    for {
      idAsList <- el.value.get("identifier")
      mayBeId = if (idAsList.asOpt[List[JsObject]].isDefined) {
        idAsList.as[List[JsObject]].headOption
      } else {
        idAsList.asOpt[JsObject]
      }
      id         <- mayBeId
      idValue    <- id.value.get("value")
      idValueStr <- idValue.asOpt[String]
    } yield idValueStr
  }

  def indexJson(
    apiName: String,
    el: JsObject,
    identifier: Option[String],
    simulate: Boolean,
    dbScope: DatabaseScope,
    releaseOnly: Boolean,
    completeRebuild: Boolean
  ): Task[Either[ApiError, Unit]] = {
    identifier match {
      case Some(id) =>
        import java.text.DateFormat
        import java.text.SimpleDateFormat
        import java.util.TimeZone
        val tz = TimeZone.getTimeZone("UTC")
        val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
        df.setTimeZone(tz)
        val nowAsISO = df.format(new Date())
        val jsonWithTimeStamp = el ++ Json.obj("@timestamp" -> nowAsISO)
        val indexed = if (!simulate) {
          elasticSearch.index(jsonWithTimeStamp, apiName, id, dbScope)
        } else {
          Task.pure(Right(()))
        }
        indexed.map {
          case Right(_) =>
            siteMapGenerator.addUrl(apiName, identifier, releaseOnly, completeRebuild)
            Right(())
          case Left(e) => Left(e)
        }
      case None =>
        Task.pure(
          Left(
            ApiError(
              INTERNAL_SERVER_ERROR,
              s"Was not able to upload json because it hasn't provided an id: ${el.toString()}"
            )
          )
        )
    }
  }

  private def reindexInstances(
    relevantType: List[String],
    organizations: List[String],
    databaseScope: DatabaseScope,
    simulate: Boolean,
    releasedOnly: Boolean,
    completeRebuild: Boolean,
    token: String
  ): List[Task[Either[ApiError, Unit]]] = {
    relevantType.map { t =>
      val template = TemplateType(t)
      queryByType(template, databaseScope, PaginationParams(None, None), organizations, token).flatMap {
        case Right(value) =>
          val valueArr = value.as[List[JsObject]]
          logger.debug(s"$template - Indexing started")
          val indexedList = for {
            el <- valueArr
          } yield {
            indexJson(
              template.apiName,
              el,
              extractIdentifier(el),
              simulate,
              databaseScope,
              releasedOnly,
              completeRebuild
            )
          }
          Task.gather(indexedList).map { _ =>
            logger.debug(s"$template - Indexing done")
            Right(())
          }
        case Left(e) => Task.pure(Left(e))
      }
    }
  }

  private def removeNonexistingItems(
    indexName: String,
    indexTime: String,
    completeRebuild: Boolean
  ): Task[Either[ApiError, Unit]] = {
    import monix.execution.Scheduler.Implicits.global
    import scala.concurrent.duration._
    elasticSearch.getNotUpdatedInstances(indexName, indexTime).map {
      case Right(Nil) =>
        Right(())
      case Right(l) =>
        var listOfIdsToRemove = l
        while (listOfIdsToRemove.nonEmpty) {
          removeIndex(listOfIdsToRemove, indexName, indexTime)
          val res = Await.result(elasticSearch.getNotUpdatedInstances(indexName, indexTime).runToFuture, 10.seconds)
          listOfIdsToRemove = res.toOption.getOrElse(List())
        }
        Right(())

      case Left(e) => Left(e)
    }
  }

  private def removeIndex(ids: List[String], indexName: String, indexTime: String) = {
    val idsToRemove = for {
      id <- ids
    } yield elasticSearch.removeIndex(id, indexName)
    Task.sequence(idsToRemove)
  }

  private def indexWithTypes(
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    relevantTypes: List[String],
    token: String
  ): Task[Either[ApiError, Unit]] = {
    createESmapping(relevantTypes, token).flatMap { mapping =>
      import java.text.SimpleDateFormat
      import java.util.TimeZone
      val tz = TimeZone.getTimeZone("UTC")
      val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
      df.setTimeZone(tz)
      val nowAsISO = df.format(new Date())
      val recreateIndexTask = if (completeRebuild && !simulate) {
        elasticSearch.recreateIndex(mapping, databaseScope)
      } else {
        Task.pure(Right(()))
      }
      recreateIndexTask.flatMap {
        case Right(()) =>
          val result = Task
            .gather(
              reindexInstances(
                relevantTypes,
                organizations,
                databaseScope,
                simulate = simulate,
                releasedOnly,
                completeRebuild,
                token
              )
              //              writeSiteMap <- siteMapGenerator.write(
              //                Path.of("/opt/scripts/kg_indexer/kg_indexer/sitemap.xml")
              //              )
            )
            .flatMap(t => {
              removeNonexistingItems(databaseScope.toIndexName, nowAsISO, completeRebuild)
            })
          result

        case Left(e) => Task.pure(Left(e))
      }
    }
  }

  override def index(
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    token: String
  ): Task[Either[ApiError, Unit]] = {
    getRelevantTypes(token).flatMap {
      case Right(relevantTypes) =>
        indexWithTypes(completeRebuild, releasedOnly, simulate, organizations, databaseScope, relevantTypes, token)
      case Left(e) => Task.pure(Left(e))
    }
  }

  override def indexByType(
    completeRebuild: Boolean,
    releasedOnly: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    relevantType: String,
    token: String
  ): Task[Either[ApiError, Unit]] = {
    indexWithTypes(completeRebuild, releasedOnly, simulate, organizations, databaseScope, List(relevantType), token)
  }

  override def transform(jsonContent: JsValue, template: Template): JsValue = {
    templateEngine.transform(jsonContent, template)
  }
  override def transformMeta(jsonContent: JsValue, template: Template): JsValue = {
    templateEngine.transformMeta(jsonContent, template)
  }

  override def queryByType(
    templateType: TemplateType,
    databaseScope: DatabaseScope,
    paginationParams: PaginationParams,
    restrictToOrgs: List[String],
    token: String
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    val paramsSize = paginationParams.limit.map(p => s"&size=${p}").getOrElse("")
    val paramsFrom = paginationParams.offset.map(p => s"&start=${p}").getOrElse("")
    val restrictOrgsToString =
      if (restrictToOrgs.isEmpty) "" else s"""&restrictToOrganizations=${restrictToOrgs.mkString(",")}"""
    logger.debug(s"$templateType - Fetching of data started")
    Task
      .fromFuture(
        WSClient
          .url(
            s"$queryEndpoint/query/$schema/search/instances/?vocab=https://schema.hbp.eu/search/&databaseScope=${databaseScope.toString}$paramsSize$paramsFrom$restrictOrgsToString"
          )
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val maybeListOfResults = for {
              jsonResponse <- wsresult.json.asOpt[Map[String, JsValue]]
              results      <- jsonResponse.get("results")
              resultArray  <- results.asOpt[List[JsObject]]
            } yield resultArray

            maybeListOfResults match {
              case Some(listOfResults) =>
                logger.debug(s"$templateType - Fetching of data done with ${listOfResults.size} elements fetched")
                val template = templateEngine.getTemplateFromType(templateType, databaseScope)
                val result = listOfResults.map(r => {
                  transform(r, template)
                })
                Right(Json.toJson(result))
              case None => Left(ApiError(INTERNAL_SERVER_ERROR, "Could not unpack json result"))
            }
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def queryByTypeAndId(
    templateType: TemplateType,
    id: UUID,
    databaseScope: DatabaseScope,
    token: String
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search/instances/${id.toString}?vocab=https://schema.hbp.eu/search/")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val template = templateEngine.getTemplateFromType(templateType, databaseScope)
            Right(transform(wsresult.json, template))
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def metaByType(
    templateType: TemplateType,
    token: String,
    fieldsOnly: Boolean = true
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val metaTemplate = templateEngine.getMetaTemplateFromType(templateType)
            val value = transformMeta(wsresult.json, metaTemplate)
            val res = if (fieldsOnly) {
              value.asOpt[JsObject].get("fields")
            } else {
              value
            }
            Right(res)
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def getLabels(templateType: TemplateType, token: String): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search")
          .addHttpHeaders("Authorization" -> s"Bearer $token")
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val template = templateEngine.getESTemplateFromType(templateType)
            Right(transformMeta(wsresult.json, template))
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

}
