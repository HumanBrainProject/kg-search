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
import java.time.{Instant, ZoneOffset}
import java.util.{Date, UUID}

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.{DatabaseScope, INFERRED, PaginationParams, RELEASED}
import models.errors.ApiError
import models.templates.{Template, TemplateType}
import monix.eval.Task
import monix.reactive.Observable
import org.slf4j.LoggerFactory
import play.api.{Configuration, Logging}
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.Status._

import scala.concurrent.Await

@ImplementedBy(classOf[IndexerImpl])
trait Indexer[Content, TransformedContent, Effect[_], UploadResult, QueryResult] {
  val batchSize = 10
  def transform(jsonContent: Content, template: Template): TransformedContent
  def transformMeta(jsonContent: Content, template: Template): TransformedContent
  def getRelevantTypes(): Effect[Either[ApiError, List[TemplateType]]]

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
    fieldsOnly: Boolean = true
  ): Effect[QueryResult]

  def getLabelsByType(
    templateType: TemplateType,
  ): Effect[Either[ApiError, (String, JsValue)]]

  def getLabels(
    templateTypes: List[TemplateType],
  ): Effect[List[Either[ApiError, (String, JsValue)]]]

  def index(
    completeRebuild: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    token: String
  ): Effect[List[Either[ApiError, Unit]]]

  def indexByType(
    completeRebuild: Boolean,
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

  def getRelevantTypes(): Task[Either[ApiError, List[TemplateType]]] = {
    Task
      .deferFuture(
        WSClient.url(s"${queryEndpoint}/query/search/schemas").get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            Right(
              wsresult.json
                .as[List[JsObject]]
                .map(js => js.value.get("https://schema.hbp.eu/relativeUrl").get.as[String])
                .sorted
                .map(TemplateType.fromSchema)
                .collect { case Some(t) => t }
            )
          case s => Left(ApiError(s, wsresult.body))
        }
      }

  }

  def createLabels(relevantTypes: List[String]): Task[Map[String, JsValue]] = {
    val labelsPerRelevantTypes = relevantTypes.map { t =>
      val template = TemplateType(t.splitAt(t.lastIndexOf("/"))._2)
      metaByType(template, fieldsOnly = false)
        .map {
          case Right(jsValue) =>
            //TODO add specification properties
            Right((template.apiName, jsValue))
          case Left(e) => Left(e)
        }
    }
    Task.sequence(labelsPerRelevantTypes).map { l =>
      l.collect { case Right(v) => v }
        .foldLeft(Map[String, JsValue]("serviceUrl" -> JsString(serviceUrlBase))) {
          case (acc, (apiName, content)) =>
            acc.updated(apiName, content)
        }
    }

  }

  private def getListOfESmapping(relevantTypes: List[TemplateType]) = {
    Task.sequence(relevantTypes.map { templateType =>
      val schema = TemplateType.toSchema(templateType)
      Task
        .deferFuture(
          WSClient
            .url(s"$queryEndpoint/query/$schema/search")
            .get()
        )
        .map { wsresult =>
          wsresult.status match {
            case OK =>
              val metaTemplate = templateEngine.getESTemplateFromType(templateType)
              val value = (templateType.apiName, transformMeta(wsresult.json, metaTemplate))
              Right(value)
            case status => Left(ApiError(status, wsresult.body))
          }
        }

    })
  }

  private def getESmapping(
    relevantTypes: List[TemplateType]
  ): Task[Map[String, Map[String, JsValue]]] = {
    logger.debug("Started - Creating ES Mapping")
    getListOfESmapping(relevantTypes).map { listOfLabelsPerType =>
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

  override def getLabels(
    relevantTypes: List[TemplateType],
  ): Task[List[Either[ApiError, (String, JsValue)]]] = {
    Task.sequence(relevantTypes.map { t =>
      getLabelsByType(t)
    })
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
    completeRebuild: Boolean
  ): Task[Either[ApiError, Unit]] = {
    identifier match {
      case Some(id) =>
        import java.text.DateFormat
        import java.text.SimpleDateFormat
        import java.util.TimeZone

        val nowAsISO = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime.toString
        val jsonWithTimeStamp = el ++ Json.obj("@timestamp" -> nowAsISO)
        val indexed = elasticSearch.index(jsonWithTimeStamp, apiName, id, dbScope, simulate)
        indexed.map {
          case Right(_) =>
            siteMapGenerator.addUrl(apiName, identifier, dbScope, completeRebuild)
            Right(())
          case Left(e) =>
            logger.debug(s"$apiName - Error while indexing - $id - ${e.status}: ${e.message}")
            Left(e)

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
    relevantType: List[TemplateType],
    organizations: List[String],
    databaseScope: DatabaseScope,
    simulate: Boolean,
    completeRebuild: Boolean,
    token: String
  ): Task[List[Either[ApiError, Unit]]] = {
    logger.debug("Started - Reindexing instances")
    val instanceIndexed: List[Task[List[Either[ApiError, Unit]]]] = relevantType.map { template =>
      queryByType(template, databaseScope, PaginationParams(None, None), organizations, token).flatMap {
        case Right(value) =>
          val valueArr = value.as[List[JsObject]]
          logger.debug(s"$template - Indexing started")
          val indexedList: List[Task[Either[ApiError, Unit]]] = for {
            el <- valueArr
          } yield {
            indexJson(
              template.apiName,
              el,
              extractIdentifier(el),
              simulate,
              databaseScope,
              completeRebuild
            )
          }

          Task.sequence(indexedList).map { s =>
            logger.debug(s"Done - Reindexing instances for ${template.apiName}")
            s
          }
        case Left(e) => Task.pure(List(Left(e)))
      }
    }
    Task.sequence(instanceIndexed).map(_.flatten.toList)
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

  private def removeIndex(
    ids: List[String],
    indexName: String,
    indexTime: String
  ): Task[List[Either[ApiError, Unit]]] = {
    val idsToRemove = for {
      id <- ids
    } yield elasticSearch.removeIndex(id, indexName)
    Task.sequence(idsToRemove)
  }

  private def indexWithTypes(
    completeRebuild: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    relevantTypes: List[TemplateType],
    token: String
  ): Task[List[Either[ApiError, Unit]]] = {
    getESmapping(relevantTypes).flatMap { mapping =>
      import java.text.SimpleDateFormat
      import java.util.TimeZone
      val tz = TimeZone.getTimeZone("UTC")
      val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
      df.setTimeZone(tz)
      val nowAsISO = df.format(new Date())
      val recreateIndexTask =
        elasticSearch.recreateIndex(mapping, databaseScope, completeRebuild, simulate)
      recreateIndexTask.flatMap {
        case Right(()) =>
          val result =
            reindexInstances(
              relevantTypes,
              organizations,
              databaseScope,
              simulate = simulate,
              completeRebuild,
              token
            )
          //              writeSiteMap <- siteMapGenerator.write(
          //                Path.of("/opt/scripts/kg_indexer/kg_indexer/sitemap.xml")
          //              )

          result.flatMap { l =>
            logger.debug("Done - Reindexing instances")
            removeNonexistingItems(databaseScope.toIndexName, nowAsISO, completeRebuild).map(_ => l)
          }
        case Left(e) => Task.pure(List(Left(e)))
      }
    }
  }

  override def index(
    completeRebuild: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    token: String
  ): Task[List[Either[ApiError, Unit]]] = {
    getRelevantTypes().flatMap {
      case Right(relevantTypes) =>
        indexWithTypes(
          completeRebuild,
          simulate,
          organizations,
          databaseScope,
          relevantTypes,
          token
        )
      case Left(e) => Task.pure(List(Left(e)))
    }
  }

  override def indexByType(
    completeRebuild: Boolean,
    simulate: Boolean,
    organizations: List[String],
    databaseScope: DatabaseScope,
    relevantType: String,
    token: String
  ): Task[Either[ApiError, Unit]] = {
    indexWithTypes(
      completeRebuild,
      simulate,
      organizations,
      databaseScope,
      List(TemplateType(relevantType)),
      token
    ).map(_.head)
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
      .deferFuture(
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
    fieldsOnly: Boolean = true
  ): Task[Either[ApiError, JsValue]] = {
    val schema = TemplateType.toSchema(templateType)
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search")
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

  override def getLabelsByType(templateType: TemplateType): Task[Either[ApiError, (String, JsValue)]] = {
    metaByType(templateType, false).map {
      case Right(js) => Right((templateType.apiName, js))
      case Left(e)   => Left(e)
    }
  }

}
