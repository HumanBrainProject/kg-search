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

import java.text.SimpleDateFormat
import java.time.{Instant, ZoneOffset}
import java.util.{Date, TimeZone, UUID}

import akka.Done
import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.errors.ApiError
import models.templates.{Template, TemplateType}
import models.{DatabaseScope, PaginationParams}
import monix.eval.Task
import play.api.cache.AsyncCacheApi
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logging}
import play.cache.NamedCache

import scala.concurrent.Await
import scala.concurrent.duration._

@ImplementedBy(classOf[IndexerImpl])
trait Indexer[Content, TransformedContent, Effect[_], UploadResult, QueryResult] {
  val maxCleanupLoops = 1000
  val batchSize = 10

  def transform(jsonContent: Content, template: Template): TransformedContent

  def transformMeta(jsonContent: Content, template: Template): TransformedContent

  def queryByType(
                   templateType: TemplateType,
                   databaseScope: DatabaseScope,
                   paginationParams: PaginationParams,
                   restrictToOrgs: List[String],
                   token: String
                 ): Effect[QueryResult]

  def queryBySchemaAndId(
                          org: String,
                          domain: String,
                          schema: String,
                          version: String,
                          id: UUID,
                          databaseScope: DatabaseScope,
                          token: String,
                          liveMode: Boolean
                        ): Effect[QueryResult]

  def clearCache(): Task[Done]

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
             organizations: List[String],
             databaseScope: DatabaseScope,
             token: String
           ): Effect[List[Either[ApiError, Unit]]]

  def indexByType(
                   completeRebuild: Boolean,
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
                             configuration: Configuration,
                             @NamedCache("search-metadata-cache") cache: AsyncCacheApi
                           ) extends Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]]
  with Logging {

  val queryEndpoint: String = configuration.get[String]("kgquery.endpoint")

  def clearCache(): Task[Done] = {
    logger.info("Now clearing the cache")
    Task.deferFuture(cache.removeAll())
  }


  def createLabels(relevantTypes: List[String]): Task[Map[String, JsValue]] = {
    logger.info(s"Creating labels")
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
        .foldLeft(Map[String, JsValue]()) {
          case (acc, (apiName, content)) =>
            acc.updated(apiName, content)
        }
    }

  }

  private def fetchESmapping(templateType: TemplateType): Task[Either[ApiError, (String, JsValue)]] = {
    //The last defined schema defines the ES mapping
    val schema = TemplateType.toSchema(templateType).last
    logger.info(s"Loading instances for schema $schema from KG Query")
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$schema/search").get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            val metaTemplate = templateEngine.getESTemplateFromType(templateType)
            val value = (templateType.apiName, transformMeta(wsresult.json, metaTemplate))
            logger.info(s"Received instances for schema $schema from KG Query: $value")
            Right(value)
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  private def createESmapping(
                               relevantType: TemplateType
                             ): Task[Either[ApiError, JsValue]] = {
    logger.info("Started - Creating ES Mapping")
    fetchESmapping(relevantType).map {
      case Right(v) =>
        logger.info("Done - Creating ES Mapping")
        Right(Json.obj("mappings" -> Json.obj("properties" -> v._2)))
      case Left(err) => Left(err)
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
      id <- mayBeId
      idValue <- id.value.get("value")
      idValueStr <- idValue.asOpt[String]
    } yield idValueStr
  }

  def indexJson(
                 apiName: String,
                 el: JsObject,
                 identifier: Option[String],
                 dbScope: DatabaseScope,
                 completeRebuild: Boolean
               ): Task[Either[ApiError, Unit]] = {
    identifier match {
      case Some(id) =>
        val nowAsISO = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime.toString
        val jsonWithTimeStampAndDocType = el ++ Json.obj("@timestamp" -> nowAsISO, "type" -> Json.obj("value" -> apiName))
        val indexed = elasticSearch.index(jsonWithTimeStampAndDocType, apiName, id, dbScope)
        indexed.map {
          case Right(_) =>
            siteMapGenerator.addUrl(apiName, identifier, dbScope, completeRebuild)
            Right(())
          case Left(e) =>
            logger.info(s"$apiName - Error while indexing - $id - ${e.status}: ${e.message}")
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
                                relevantType: TemplateType,
                                organizations: List[String],
                                databaseScope: DatabaseScope,
                                completeRebuild: Boolean,
                                token: String
                              ): Task[List[Either[ApiError, Unit]]] = {
    logger.info("Started - Reindexing instances")
    queryByType(relevantType, databaseScope, PaginationParams(None, None), organizations, token).flatMap {
      case Right(value) =>
        val valueArr = value.as[List[JsObject]]
        if (valueArr.isEmpty) {
          Task.pure(List(Left(ApiError(NOT_FOUND, s"There were no elements for the type ${relevantType.apiName} - not doing anything..."))))
        }
        else {
          logger.info(s"$relevantType - Indexing started")
          val indexedList: List[Task[Either[ApiError, Unit]]] = for {
            el <- valueArr
          } yield {
            indexJson(
              relevantType.apiName,
              el,
              extractIdentifier(el),
              databaseScope,
              completeRebuild
            )
          }
          Task.sequence(indexedList).map { s =>
            logger.info(s"Done - Reindexing instances for ${relevantType.apiName}")
            s
          }
        }
      case Left(e) => Task.pure(List(Left(e)))
    }
  }

  private def removeNonexistingItems(
                                      indexName: String,
                                      indexTime: String,
                                      completeRebuild: Boolean
                                    ): Task[Either[ApiError, Unit]] = {
    import monix.execution.Scheduler.Implicits.global

    import scala.concurrent.duration._
    if (!completeRebuild) {
      Thread.sleep(10000) // wait for 10 seconds
      elasticSearch.getNotUpdatedInstances(indexName, indexTime).map {
        case Right(Nil) =>
          Right(())
        case Right(l) =>
          var listOfIdsToRemove = l
          var loops = 0
          while (loops < maxCleanupLoops && listOfIdsToRemove.nonEmpty) {
            loops += 1
            logger.debug(s"Found ${listOfIdsToRemove.size} instances which have to be removed from the ES index")
            Await.result(removeIndex(listOfIdsToRemove, indexName, indexTime).runToFuture, 20.seconds)
            Thread.sleep(2000) // wait for 2 seconds
            logger.debug("Removed elements which have not been updated during last run")
            val res = Await.result(elasticSearch.getNotUpdatedInstances(indexName, indexTime).runToFuture, 10.seconds)
            listOfIdsToRemove = res.toOption.getOrElse(List())
          }
          Right(())

        case Left(e) => Left(e)
      }
    }
    else {
      Task.pure(Right(()))
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

  private def indexWithType(
                             completeRebuild: Boolean,
                             organizations: List[String],
                             databaseScope: DatabaseScope,
                             relevantType: TemplateType,
                             token: String,
                             nowAsISO: String
                           ): Task[Either[ApiError, Unit]] = {
    createESmapping(relevantType).flatMap {
      case Right(mappingValue) =>
        val recreateIndexTask = elasticSearch.recreateIndex(mappingValue, relevantType, databaseScope, completeRebuild)
        recreateIndexTask.flatMap {
          case Right(()) =>
            val result = reindexInstances(
              relevantType,
              organizations,
              databaseScope,
              completeRebuild,
              token
            )
            result.flatMap {
              l => {
                logger.debug("Done - Reindexing instances")
                val errors = l.collect { case Left(e) => e }
                if (errors.isEmpty) {
                  removeNonexistingItems(s"${databaseScope.toIndexName}_${relevantType.apiName.toLowerCase}", nowAsISO, completeRebuild)
                }
                else {
                  logger.error("There were errors during the indexing process - we're not removing the elements which have not been reported")
                  Task.pure(Left(ApiError(INTERNAL_SERVER_ERROR, "There were errors during the indexing process")))
                }
              }
            }
          case Left(e) => Task.pure(Left(e))
        }
      case Left(err) => Task.pure(Left(err))
    }
  }

  override def index(
                      completeRebuild: Boolean,
                      organizations: List[String],
                      databaseScope: DatabaseScope,
                      token: String
                    ): Task[List[Either[ApiError, Unit]]] = {
    val tz = TimeZone.getTimeZone("UTC")
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.setTimeZone(tz)
    val nowAsISO = df.format(new Date())
    Task.sequence(
      TemplateType.orderedList()
        .map(relType =>
          indexWithType(
            completeRebuild,
            organizations,
            databaseScope,
            relType,
            token,
            nowAsISO
          )
        )
    )
  }

  override def indexByType(
                            completeRebuild: Boolean,
                            organizations: List[String],
                            databaseScope: DatabaseScope,
                            relevantType: String,
                            token: String
                          ): Task[Either[ApiError, Unit]] = {
    val tz = TimeZone.getTimeZone("UTC")
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.setTimeZone(tz)
    val nowAsISO = df.format(new Date())
    indexWithType(
      completeRebuild,
      organizations,
      databaseScope,
      TemplateType(relevantType),
      token,
      nowAsISO
    )
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
    val schemas = TemplateType.toSchema(templateType)
    val paramsSize = paginationParams.limit.map(p => s"&size=${p}").getOrElse("")
    val paramsFrom = paginationParams.offset.map(p => s"&start=${p}").getOrElse("")
    val restrictOrgsToString =
      if (restrictToOrgs.isEmpty) "" else s"""&restrictToOrganizations=${restrictToOrgs.mkString(",")}"""
    logger.info(s"$templateType - Fetching of data started")

    val fetchTasks = schemas.map {
      schema =>
        Task.deferFuture(
          WSClient
            .url(
              s"$queryEndpoint/query/$schema/search/instances/?vocab=https://schema.hbp.eu/search/&databaseScope=${databaseScope.toString}$paramsSize$paramsFrom$restrictOrgsToString"
            )
            .addHttpHeaders("Authorization" -> token)
            .get()
        )
    }
    Task.sequence(fetchTasks).map(
      wsResults => {
        val payloads = wsResults.map {
          wsResult =>
            wsResult.status match {
              case OK =>
                Right((wsResult.json \ "results").asOpt[List[JsObject]].getOrElse(List()))
              case status =>
                Left(ApiError(status, wsResult.body))
            }
        }
        val errors = payloads.collect { case Left(e) => e }
        if (errors.isEmpty) {
          val objects = payloads.collect { case Right(payload) => payload }.foldLeft(Map[String, JsObject]()) {
            (acc, resultList) =>
              for (el <- resultList) {
                (el \ "identifier").asOpt[String] match {
                  case Some(identifier) => acc + (identifier -> el)
                  case _ => None
                }
              }
              acc
          }.valuesIterator.toList
          logger.info(s"$templateType - Fetching of data done with ${objects.size} elements fetched")
          val template = templateEngine.getTemplateFromType(templateType, databaseScope, false)
          val result = objects.map(r => {
            transform(r, template)
          })
          if (result.isEmpty) {
            Left(ApiError(NOT_FOUND, "Did not find any objects - don't do anything..."))
          }
          else {
            Right(Json.toJson(result))
          }
        }
        else {
          Left(ApiError(INTERNAL_SERVER_ERROR, "Errors while fetching payloads"))
        }
      }
    )
  }

  override def queryBySchemaAndId(
                                   org: String,
                                   domain: String,
                                   schema: String,
                                   version: String,
                                   id: UUID,
                                   databaseScope: DatabaseScope,
                                   token: String,
                                   liveMode: Boolean
                                 ): Task[Either[ApiError, JsValue]] = {
    Task
      .deferFuture(
        WSClient
          .url(s"$queryEndpoint/query/$org/$domain/$schema/$version/search/instances/${id.toString}?vocab=https://schema.hbp.eu/search/")
          .addHttpHeaders("Authorization" -> token)
          .get()
      )
      .map { wsresult =>
        wsresult.status match {
          case OK =>
            TemplateType.fromSchema(s"$org/$domain/$schema/$version") match {
              case Some(t) =>
                val template = templateEngine.getTemplateFromType(t, databaseScope, liveMode)
                Right(transform(wsresult.json, template))
              case _ =>
                Left(ApiError(NOT_FOUND, s"Did not find template type for schema $org/$domain/$schema/$version"))
            }
          case status => Left(ApiError(status, wsresult.body))
        }
      }
  }

  override def metaByType(
                           templateType: TemplateType,
                           fieldsOnly: Boolean = true
                         ): Task[Either[ApiError, JsValue]] = {

    //The last declaration of the template types defines the meta information
    val schema = TemplateType.toSchema(templateType).last
    Task.deferFuture(cache.get[JsValue](s"meta-${schema}")).flatMap {
      case Some(elem) =>
        logger.info(s"Found meta data for ${schema} in cache")
        Task.pure(Right(elem))
      case None =>
        logger.info(s"Didn't find meta data for ${schema}  -> fetching")
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
                cache.set(s"meta-${schema}", res, 24.hours)
                Right(res)
              case status => Left(ApiError(status, wsresult.body))
            }
          }
    }
  }

  override def getLabelsByType(templateType: TemplateType): Task[Either[ApiError, (String, JsValue)]] = {
    metaByType(templateType, false).map {
      case Right(js) => Right((templateType.apiName, js))
      case Left(e) => Left(e)
    }
  }

}
