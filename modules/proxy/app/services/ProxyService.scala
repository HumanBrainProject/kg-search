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

package services

import akka.util.ByteString
import com.google.inject.Inject
import helpers.ESHelper
import play.api.Logger
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{AnyContent, BodyParser, RawBuffer, Request}

import scala.concurrent.ExecutionContext

class ProxyService @Inject()(wSClient: WSClient)(implicit executionContext: ExecutionContext) {
  val logger: Logger = Logger(this.getClass)

  def queryIndex(
    esIndex: String,
    proxyUrl: String,
    es_host: String,
    transformInputFunc: ByteString => ByteString = identity
  )(implicit request: Request[AnyContent], executionContext: ExecutionContext): (WSRequest, Map[String, String]) = {
    val newUrl = es_host + "/" + ESHelper.replaceESIndex(esIndex, proxyUrl)
    logger.debug(s"Modified URL: $newUrl")
    val wsRequestBase: WSRequest = modifyQuery(newUrl, esIndex)
    // depending on whether we have a body, append it in our request
    val byteString = for {
      raw <- request.body.asJson
    } yield ByteString(raw.toString())

    val wsRequest: WSRequest = byteString match {
      case Some(bytes) => wsRequestBase.withBody(transformInputFunc(bytes))
      case None        => wsRequestBase
    }
    (wsRequest, Map("X-Selected-Index" -> esIndex))
  }

  def modifyQuery(newUrl: String, indexHint: String)(implicit request: Request[AnyContent]): WSRequest = {
    wSClient
      .url(newUrl) // set the proxy path
      .withMethod(request.method) // set our HTTP  method
      .addHttpHeaders("index-hint" -> indexHint) // Set our headers, function takes var args so we need to "explode" the Seq to var args
      .addQueryStringParameters(request.queryString.mapValues(_.head).toSeq: _*) // similarly for query strings
  }

}
