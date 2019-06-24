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
import services.OIDCAuthService
import helpers.{ESHelper, OIDCHelper, ResponseHelper}
import javax.inject.{Inject, Singleton}
import monix.eval.Task
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, ResponseHeader, Result}
import play.api.{Configuration, Logger}
import services.ProxyService
import utils.JsonHandler

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchProxy @Inject()(
  cc: ControllerComponents,
  mat: Materializer,
  authService: OIDCAuthService,
  proxyService: ProxyService
)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
    extends AbstractController(cc) {
  val es_host = config.get[String]("es.host")
  implicit val s = monix.execution.Scheduler.Implicits.global

  val logger: Logger = Logger(this.getClass)

  def proxy(indexWithProxyUrl: String): Action[AnyContent] = Action.async { implicit request =>
    val segments = indexWithProxyUrl.split("/")
    val (index, proxyUrl) = (segments.head, segments.tail.mkString("/"))
    processRequest(index, proxyUrl).runToFuture
  }

  def proxyOptions(index: String, proxyUrl: String): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "GET, POST, OPTIONS")
  }

  def processRequest(index: String, proxyUrl: String, transformInputFunc: ByteString => ByteString = identity)(
    implicit request: Request[AnyContent]
  ): Task[Result] = {
    logger.debug(s"Search proxy - Index: $index, proxy: $proxyUrl")
    val searchTerm = request.body.asJson.getOrElse(Json.obj()).as[JsValue]
    val t = (searchTerm \ "query" \\ "query").headOption.map(_.asOpt[String].getOrElse("")).getOrElse("")
    val opts: (Option[String], Option[String]) =
      (request.headers.get("index-hint"), request.headers.get("Authorization"))
    opts match {
      case (Some(hints), Some(auth)) =>
        authService.getUserInfo(request.headers).flatMap {
          case Some(userInfo) =>
            val esIndex = OIDCHelper.getESIndex(userInfo, hints)
            val (wsRequest, index) = proxyService.queryIndex(esIndex, proxyUrl, es_host, transformInputFunc)
            propagateRequest(wsRequest, index, t)
          case _ =>
            val (wsRequest, index) =
              proxyService.queryIndex(ESHelper.publicIndex, proxyUrl, es_host, transformInputFunc)
            propagateRequest(wsRequest, index, t)
        }
      case _ =>
        val (wsRequest, index) = proxyService.queryIndex(ESHelper.publicIndex, proxyUrl, es_host, transformInputFunc)
        propagateRequest(wsRequest, index, t)
    }
  }

  def propagateRequest(
    request: WSRequest,
    headers: Map[String, String] = Map(),
    searchTerm: String = ""
  ): Task[Result] = {
    Task
      .deferFuture(
        request
          .execute()
      )
      .map { response => // we want to read the raw bytes for the body
        val count = (response.json \ "hits" \ "total").asOpt[Long].getOrElse(0L)
        logger.info(s"Search query - term: $searchTerm, #results: $count")
        val h = ResponseHelper
          .flattenHeaders(
            ResponseHelper.filterContentTypeAndLengthFromHeaders[Seq[String]](
              response.headers
            )
          ) ++ headers
        Result(
          // keep original response header except content type and length that need specific handling
          ResponseHeader(
            response.status,
          ),
          HttpEntity.Strict(response.bodyAsBytes, ResponseHelper.getContentType(response.headers))
        ).withHeaders(h.toList: _*)
      }
  }

  def proxySearch(indexWithProxyUrl: String): Action[AnyContent] = Action.async { implicit request =>
    val segments = indexWithProxyUrl.split("/")
    val (index, proxyUrl) = (segments.head, segments.tail.mkString("/"))
    updateEsResponseWithNestedDocument(
      processRequest(index, proxyUrl, transformInputFunc = SearchProxy.adaptEsQueryForNestedDocument)
    ).runToFuture
  }

  def updateEsResponseWithNestedDocument(response: Task[Result]): Task[Result] = {
    response.flatMap[Result] { rawResponse =>
      if (rawResponse.header.status != 200) {
        Task.pure(rawResponse)
      } else {
        // need to get full response back to process it
        Task.deferFuture(rawResponse.body.consumeData(mat)).map { bytes =>
          val json = SearchProxy.updateEsResponseWithNestedDocument(Json.parse(bytes.utf8String).as[JsObject])
          Ok(json).withHeaders(rawResponse.header.headers.toList: _*)
        }
      }
    }
  }

  def labels(proxyUrl: String): Action[AnyContent] = Action.async { implicit request =>
    val wsRequestBase: WSRequest = ws.url(es_host + "/kg_labels/" + proxyUrl)
    propagateRequest(wsRequestBase).runToFuture
  }

  def labelsOptions(proxyUrl: String): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "GET, OPTIONS")
  }
}

object SearchProxy {
  val logger: Logger = Logger(this.getClass)
  val parentCountLabel = "temporary_parent_doc_count"
  val docCountLabel = "doc_count"
  val parentDocCountObj = Json.parse(s"""{\"aggs\": {\"$parentCountLabel\": {\"reverse_nested\": {}}}}""").as[JsObject]

  def adaptEsQueryForNestedDocument(payload: ByteString): ByteString = {
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
  }

  def updateEsResponseWithNestedDocument(jsonSrc: JsObject): JsObject = {
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
}
