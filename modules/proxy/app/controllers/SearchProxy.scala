
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

package proxy.controllers

import akka.stream.Materializer
import akka.util.ByteString
import common.helpers.{ResponseHelper, ESHelper}
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.{Configuration, Logger}
import javax.inject.{Inject, Singleton}
import models.authentication.UserInfo
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, PlayBodyParsers, RawBuffer, Request, ResponseHeader, Result}
import play.api.{Configuration, Logger}
import proxy.utils.JsonHandler
import service.authentication.OIDCAuthService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
@Singleton
class SearchProxy @Inject()(
                             @NamedCache("groups-cache") cache: AsyncCacheApi,
                             cc: ControllerComponents,
                             playBodyParsers: PlayBodyParsers,
                             mat: Materializer,
                             authService: OIDCAuthService
                           )(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
  extends AbstractController(cc) {
  val es_host = config.get[String]("es.host")
  implicit val cacheExpiration = config.get[FiniteDuration]("proxy.cache.expiration")
  val logger: Logger = Logger(this.getClass)


  def proxy(index: String, proxyUrl: String): Action[RawBuffer] = Action.async(playBodyParsers.raw) {
    implicit request =>
      processRequest(index, proxyUrl)
  }

  def proxyOptions(index:String, proxyUrl:String): Action[AnyContent] = Action {
    Ok("").withHeaders("Allow" -> "GET, POST, OPTIONS")
  }

  def processRequest(index: String, proxyUrl: String, transformInputFunc: ByteString => ByteString = identity)(implicit request: Request[RawBuffer]): Future[Result]  = {
    logger.debug(s"Index: $index, proxy: $proxyUrl")
    val opts: (Option[String], Option[String]) = (request.headers.get("index-hint"), request.headers.get("Authorization"))
    opts match {
      case (Some(hints), Some(auth)) =>
        SearchProxy.retrieveNexusGroups(auth, cache, authService).flatMap {
          case Some(userInfo) =>
            val esIndex = SearchProxy.getESIndex(userInfo, hints)
            SearchProxy.queryIndexAndPropagate(esIndex, proxyUrl, es_host, transformInputFunc)
          case _ =>
            SearchProxy.queryIndexAndPropagate(ESHelper.publicIndex, proxyUrl, es_host, transformInputFunc)
        }
      case _ => SearchProxy.queryIndexAndPropagate(ESHelper.publicIndex, proxyUrl, es_host, transformInputFunc)
    }
  }

  def proxySearch(index: String, proxyUrl: String): Action[RawBuffer] = Action.async(playBodyParsers.raw) { implicit request =>
    logger.debug(s"smartproxy query - Index: $index, proxy: $proxyUrl")
    updateEsResponseWithNestedDocument(
      processRequest(index, proxyUrl, transformInputFunc = SearchProxy.adaptEsQueryForNestedDocument))
  }

  def updateEsResponseWithNestedDocument(response: Future[Result]): Future[Result] = {
    response.flatMap[Result] {
      rawResponse =>
        if (rawResponse.header.status != 200) {
          Future(rawResponse)
        } else {
          // need to get full response back to process it
          rawResponse.body.consumeData(mat).map {
            bytes =>
              val json = SearchProxy.updateEsResponseWithNestedDocument(
                Json.parse(bytes.utf8String).as[JsObject])
              Ok(json)
          }
        }
    }
  }

  def labels(proxyUrl: String): Action[AnyContent] = Action.async { request =>
    val wsRequestBase: WSRequest = ws.url(es_host + "/kg_labels/" + proxyUrl)
    SearchProxy.propagateRequest(wsRequestBase)
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

  def modifyQuery(newUrl: String, indexHint: String)(implicit ws: WSClient, request: Request[RawBuffer]): WSRequest = {
    ws.url(newUrl) // set the proxy path
      .withMethod(request.method) // set our HTTP  method
      .addHttpHeaders("index-hint" -> indexHint) // Set our headers, function takes var args so we need to "explode" the Seq to var args
      .addQueryStringParameters(request.queryString.mapValues(_.head).toSeq: _*) // similarly for query strings
  }


  def getESIndex(userInfo: UserInfo, hints:String)(implicit executionContext: ExecutionContext): String = {
    val groups = OIDCAuthService.extractNexusGroup(userInfo.groups)
    val h = hints.trim
    if (groups.contains(h)) {
      ESHelper.transformToIndex(h)
    } else ESHelper.publicIndex
  }

  def replaceESIndex(esIndex: String, proxyUrl: String): String = {
    s"$esIndex/$proxyUrl"
  }

  def queryIndexAndPropagate(esIndex: String, proxyUrl: String, es_host: String, transformInputFunc: ByteString => ByteString = identity)(implicit request: Request[RawBuffer], ws: WSClient, executionContext: ExecutionContext) = {
    val newUrl = es_host + "/" + SearchProxy.replaceESIndex(esIndex, proxyUrl)
    logger.debug(s"Modified URL: $newUrl")
    val wsRequestBase: WSRequest = SearchProxy.modifyQuery(newUrl, esIndex)(ws, request)
    // depending on whether we have a body, append it in our request

    val wsRequest: WSRequest = request.body.asBytes() match {
      case Some(bytes) =>
        wsRequestBase.withBody(transformInputFunc(bytes))
      case None => wsRequestBase
    }
    SearchProxy.propagateRequest(wsRequest, Map("selected_index" -> esIndex))
  }

  def propagateRequest(request: WSRequest, headers: Map[String, String] = Map())(implicit executionContext: ExecutionContext): Future[Result] = {
    request
      .execute()
      .map { case (response: request.Response) => // we want to read the raw bytes for the body
      Result(
        // keep original response header except content type and length that need specific handling
        ResponseHeader(response.status, ResponseHelper.flattenHeaders(ResponseHelper.filterContentTypeAndLengthFromHeaders[Seq[String]](response.headers)) ++ headers),
        HttpEntity.Strict(response.bodyAsBytes, ResponseHelper.getContentType(response.headers))
      )
    }
  }

  /**
    * Retrieve groups from OIDC or from cache
    * @param auth The token from the user
    * @param cache The cache used
    * @param ec
    * @param ws
    * @param config
    * @return Either a WSResponse if the status is not successful or the groups as Json
    */
  def retrieveNexusGroups(auth: String, cache: AsyncCacheApi, authService: OIDCAuthService)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration, cacheExpiration: FiniteDuration): Future[Option[UserInfo]]  = {
    cache.get[UserInfo](auth).flatMap{
      case Some(userInfo) =>
        logger.debug(s"Groups fetched from cache ${userInfo.id}")
        Future.successful(Some(userInfo))
      case _ =>
        authService.getUserInfoFromToken(auth).map{
          case Some(userInfo) =>
            cache.set(auth, userInfo, cacheExpiration)
            Some(userInfo)
          case _ =>
            None
        }
    }
  }

  def adaptEsQueryForNestedDocument(payload: ByteString): ByteString = {
    try {
      var json = Json.parse(payload.utf8String).as[JsObject]
      // get list of nested objects
      val innerList = JsonHandler.findPathForKey("nested", __, json)
      // for each one, add a parent_count_section in aggs/terms if any
      innerList.foreach{
        path =>
          (path \ "aggs").read[JsObject].reads(json) match {
            case JsSuccess(innerAggregationSection, innerAggregationSectionPath) =>
              innerAggregationSection.fields.foreach{
                case (innerAggregationKey, innerAggregationContent) =>
                  if (innerAggregationContent.as[JsObject].keys.contains("terms")){
                    val jsonUpdater = (innerAggregationSectionPath \ innerAggregationKey).json.update(
                      __.read[JsObject].map{ o => o ++ parentDocCountObj})
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
        logger.info(s"Exception in json query adaptation. Error:\n${e.getMessage}\nInput is used:\n${Json.parse(payload.utf8String).toString}")
        payload
    }
  }

  def updateEsResponseWithNestedDocument(jsonSrc: JsObject): JsObject = {
    try {
      var json = jsonSrc
      val innerList = JsonHandler.findPathForKey(parentCountLabel, __, json)
      // keep distinct buckets only
      val buckets = innerList.map(path => JsPath(path.path.dropRight(1))).distinct
        buckets.foreach {
        bucketPath =>
          val bucketSize = bucketPath.read[JsArray].reads(json).asOpt.map(_.value.size).getOrElse(0)
          // build seq of parent count values
          val parentCounts = Range(0, bucketSize).map {
            idx =>
              (bucketPath \ idx \ parentCountLabel \ docCountLabel).read[JsValue].reads(json) match {
                case JsSuccess(count, _) => count.as[Int]
                case JsError(e) =>
                  throw new Exception(s"cannot process bucket index: ${e.toString()}")
              }
          }
          val bucketSum = parentCounts.reduce(_ + _)

          // update counts for this bucket and remove parent_doc_count section
          val jsonUpdater = (bucketPath).json.update(
            __.read[JsArray].map {
              case jsArray =>
                JsArray(
                  jsArray.value.zip(parentCounts).map{
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
          val jsonInnerCountUpdater = (JsPath(bucketPath.path.dropRight(2)).json.update(
            __.read[JsObject].map { o => o ++ Json.obj(docCountLabel -> JsNumber(bucketSum)) }))
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