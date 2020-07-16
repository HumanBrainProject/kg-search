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

package controllers

import akka.stream.Materializer
import akka.util.ByteString
import services.{IDMAPIService, ProxyService, TokenAuthService}
import helpers.{ESHelper, OIDCHelper, ResponseHelper}
import javax.inject.{Inject, Singleton}
import models.errors.ApiError
import monix.eval.Task
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, ResponseHeader, Result}
import play.api.{Configuration, Logger}
import services.indexer.Indexer
import utils.JsonHandler

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchController @Inject()(
  cc: ControllerComponents,
  mat: Materializer,
  authService: IDMAPIService,
  proxyService: ProxyService,
  indexerService: Indexer[JsValue, JsValue, Task, WSResponse, Either[ApiError, JsValue]],
)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
    extends AbstractController(cc) {
  val es_host = config.get[String]("es.host")
  implicit val s = monix.execution.Scheduler.Implicits.global

  val logger: Logger = Logger(this.getClass)
  val serviceUrlBase: String = config.get[String]("serviceUrlBase")

  def document(group: String, dataType: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    OIDCHelper.groupNeedsPermissions(group) match {
      case false => processRequest(OIDCHelper.getESIndex(group, dataType), id).runToFuture
      case true  =>
        val token = OIDCHelper.getTokenFromRequest(request)
        authService.getUserInfo(token).flatMap {
          case Some(userInfo) =>
            OIDCHelper.isUserGrantedAccessToGroup(userInfo, group) match {
              case true => processRequest(OIDCHelper.getESIndex(group, dataType), id)
              case false => Task.pure(Unauthorized(s"You are not granted access to group ${group}."))
            }
          case _ => Task.pure(Unauthorized(s"You must be logged in to execute this request."))
        }.runToFuture
    }
  }

  def documentOptions(group: String, dataType: String, id: String): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "GET, OPTIONS")
  }

  def search(group: String): Action[AnyContent] = Action.async { implicit request =>
    OIDCHelper.groupNeedsPermissions(group) match {
      case false =>
        updateEsResponseWithNestedDocument(
          processRequest(OIDCHelper.getESIndex(group, "*"), "_search", transformInputFunc = SearchController.adaptEsQueryForNestedDocument)
        ).runToFuture
      case true  =>
        val token = OIDCHelper.getTokenFromRequest(request)
        authService.getUserInfo(token).flatMap {
          case Some(userInfo) =>
            OIDCHelper.isUserGrantedAccessToGroup(userInfo, group) match {
              case true =>
                updateEsResponseWithNestedDocument(
                  processRequest(OIDCHelper.getESIndex(group, "*"), "_search", transformInputFunc = SearchController.adaptEsQueryForNestedDocument)
                )
              case false => Task.pure(Unauthorized(s"You are not granted access to group ${group}."))
            }
          case _ => Task.pure(Unauthorized(s"You must be logged in to execute this request."))
        }.runToFuture
    }
  }

  def searchOptions(group: String): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "POST, OPTIONS")
  }

  def processRequest(index: String, proxyUrl: String, transformInputFunc: ByteString => ByteString = identity)(
    implicit request: Request[AnyContent]
  ): Task[Result] = {
    logger.debug(s"Search proxy - Index: $index, proxy: $proxyUrl")
    val searchTerm = request.body.asJson.getOrElse(Json.obj()).as[JsValue]
    val t = (searchTerm \ "query" \\ "query").headOption.map(_.asOpt[String].getOrElse("")).getOrElse("")
    val wsRequest = proxyService.queryIndex(index, proxyUrl, es_host, transformInputFunc)
    propagateRequest(wsRequest, t)
  }

  def propagateRequest(
    request: WSRequest,
    searchTerm: String = ""
  ): Task[Result] =
    Task
      .deferFuture(
        request
          .execute()
      )
      .map { response => // we want to read the raw bytes for the body
        val headers: Map[String, String] = Map()
        val count = (response.json \ "hits" \ "total").asOpt[Long].getOrElse(0L)
        val responseHeaders: Map[String, scala.collection.Seq[String]] = response.headers
        logger.info(s"Search query - term: $searchTerm, #results: $count")
        val h = ResponseHelper
          .flattenHeaders(
            ResponseHelper
              .filterContentTypeAndLengthFromHeaders[scala.collection.Seq[String]](responseHeaders)
          ) ++ headers
        Result(
          // keep original response header except content type and length that need specific handling
          ResponseHeader(response.status),
          HttpEntity.Strict(response.bodyAsBytes, ResponseHelper.getContentType(responseHeaders))
        ).withHeaders(h.toList: _*)
      }

  def updateEsResponseWithNestedDocument(response: Task[Result]): Task[Result] =
    response.flatMap[Result] { rawResponse =>
      if (rawResponse.header.status != 200) {
        Task.pure(rawResponse)
      } else {
        // need to get full response back to process it
        Task.deferFuture(rawResponse.body.consumeData(mat)).map { bytes =>
          val json = SearchController.updateEsResponseWithNestedDocument(Json.parse(bytes.utf8String).as[JsObject])
          Ok(json).withHeaders(rawResponse.header.headers.toList: _*)
        }
      }
    }

  def labels(): Action[AnyContent] = Action.async { implicit request =>
    indexerService
      .getRelevantTypes()
      .flatMap {
        case Right(relevantTypes) =>
          indexerService.getLabels(relevantTypes).map { labels =>
            val errors = labels.collect { case Left(e) => e }
            if (errors.isEmpty) {
              val successful = labels.collect { case Right(l) => l }
              Ok(Json.obj("_source" -> Json.toJson(successful.toMap)))
            } else {
              val message = errors.foldLeft("") {
                case (acc, e) => s"$acc + \n Status - ${e.status} - ${e.message}"
              }
              InternalServerError(s"Multiple errors detected - $message")
            }
          }
        case Left(e) => Task.pure(e.toResults())
      }
      .runToFuture
  }

  def labelsOptions(): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "GET, OPTIONS")
  }
}

object SearchController {
  val logger: Logger = Logger(this.getClass)
  val parentCountLabel = "temporary_parent_doc_count"
  val docCountLabel = "doc_count"
  val parentDocCountObj = Json.parse(s"""{\"aggs\": {\"$parentCountLabel\": {\"reverse_nested\": {}}}}""").as[JsObject]

  def adaptEsQueryForNestedDocument(payload: ByteString): ByteString =
    try {
      var json = Json.parse(payload.utf8String).as[JsObject]
      // get list of nested objects
      val innerList = JsonHandler.findPathForKey("nested", __, json)
      // for each one, add a parent_count_section in aggs/terms if any
      innerList.foreach { path =>
        (path \ "aggs").read[JsObject].reads(json) match {
          case JsSuccess(innerAggregationSection, innerAggregationSectionPath) =>
            innerAggregationSection.fields.foreach {
              case (innerAggregationKey, innerAggregationContent) =>
                if (innerAggregationContent.as[JsObject].keys.contains("terms")) {
                  val jsonUpdater =
                    (innerAggregationSectionPath \ innerAggregationKey).json.update(__.read[JsObject].map { o =>
                      o ++ parentDocCountObj
                    })
                  json = json.transform(jsonUpdater) match {
                    case JsSuccess(newJson, _) => newJson
                    case JsError(e) =>
                      throw new Exception(s"cannot adapt inner section query: ${e.toString()}")
                  }
                }
            }
          case _ => // There is no nested aggregation. Nothing to do on this input
        }
      }
      ByteString(json.toString().getBytes)
    } catch {
      case e: Exception =>
        logger.info(
          s"Exception in json query adaptation. Error:\n${e.getMessage}\nInput is used:\n${Json.parse(payload.utf8String).toString}"
        )
        payload
    }

  def updateEsResponseWithNestedDocument(jsonSrc: JsObject): JsObject =
    try {
      var json = jsonSrc
      val innerList = JsonHandler.findPathForKey(parentCountLabel, __, json)
      // keep distinct buckets only
      val buckets = innerList.map(path => JsPath(path.path.dropRight(1))).distinct
      buckets.foreach { bucketPath =>
        val bucketSize = bucketPath.read[JsArray].reads(json).asOpt.map(_.value.size).getOrElse(0)
        // build seq of parent count values
        val parentCounts = Range(0, bucketSize).map { idx =>
          (bucketPath \ idx \ parentCountLabel \ docCountLabel).read[JsValue].reads(json) match {
            case JsSuccess(count, _) => count.as[Int]
            case JsError(e) =>
              throw new Exception(s"cannot process bucket index: ${e.toString()}")
          }
        }
        val bucketSum = parentCounts.sum

        // update counts for this bucket and remove parent_doc_count section
        val jsonUpdater = bucketPath.json.update(__.read[JsArray].map { jsArray =>
          JsArray(jsArray.value.zip(parentCounts).map {
            case (element, count) =>
              (element.as[JsObject] - parentCountLabel) ++ Json.obj(docCountLabel -> count)
          })
        })
        json = json.transform(jsonUpdater) match {
          case JsSuccess(newJson, _) => newJson
          case JsError(e) =>
            throw new Exception(s"cannot update facet count: ${e.toString()}")
        }

        // update inner count linked to this bucket
        val jsonInnerCountUpdater = JsPath(bucketPath.path.dropRight(2)).json.update(__.read[JsObject].map { o =>
          o ++ Json.obj(docCountLabel -> JsNumber(bucketSum))
        })
        json = json.transform(jsonInnerCountUpdater) match {
          case JsSuccess(newJson, _) => newJson
          case JsError(e) =>
            throw new Exception(s"cannot update inner count: ${e.toString()}")
        }
      }
      json
    } catch {
      case e: Exception =>
        logger.info(s"Exception in json response update. Error:\n${e.getMessage}\nInput is used:\n${jsonSrc.toString}")
        jsonSrc
    }
}
