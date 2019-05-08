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
package services.query

import models.{AccessToken, BasicAccessToken, NexusPath, RefreshAccessToken}
import models.instance.NexusInstanceReference
import play.api.http.HeaderNames._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.http.ContentTypes._
import services.{AuthHttpClient, CredentialsService, OIDCAuthService}

import scala.concurrent.{ExecutionContext, Future}

trait QueryService {

  def getInstancesWithId(
    wSClient: WSClient,
    apiEndpoint: String,
    nexusInstanceReference: NexusInstanceReference,
    query: String,
    token: AccessToken,
    queryApiParameters: QueryApiParameter
  )(
    implicit ex: ExecutionContext,
    OIDCAuthService: OIDCAuthService,
    credentials: CredentialsService
  ): Future[WSResponse] = {
    val q = wSClient
      .url(
        s"$apiEndpoint/query/${nexusInstanceReference.nexusPath.toString()}/instances/${nexusInstanceReference.id}"
      )
      .addQueryStringParameters(queryApiParameters.toParams: _*)
      .withHttpHeaders(CONTENT_TYPE -> JSON, AUTHORIZATION -> token.token)
    token match {
      case BasicAccessToken(_)   => q.post(query)
      case RefreshAccessToken(_) => AuthHttpClient.postWithRetry(q, query)
    }
  }

  def getInstances(
    wSClient: WSClient,
    apiEndpoint: String,
    nexusPath: NexusPath,
    query: String,
    token: AccessToken,
    queryApiParameters: QueryApiParameter
  )(
    implicit ex: ExecutionContext,
    OIDCAuthService: OIDCAuthService,
    credentials: CredentialsService
  ): Future[WSResponse] = {
    val q = wSClient
      .url(s"$apiEndpoint/query/${nexusPath.toString()}/instances")
      .addQueryStringParameters(queryApiParameters.toParams: _*)
      .withHttpHeaders(CONTENT_TYPE -> JSON, AUTHORIZATION -> token.token)
    token match {
      case BasicAccessToken(_)   => q.post(query)
      case RefreshAccessToken(_) => AuthHttpClient.postWithRetry(q, query)
    }

  }

}
