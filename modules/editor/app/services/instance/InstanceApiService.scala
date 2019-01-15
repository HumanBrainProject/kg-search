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

package services.instance

import constants.{EditorClient, EditorConstants, ServiceClient}
import models.NexusPath
import models.errors.APIEditorError
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.user.User
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._
import play.api.http.HeaderNames._

trait InstanceApiService {
  val instanceEndpoint = "/internal/api/instances"

  def get(
    wSClient: WSClient,
    apiBaseEndpoint: String,
    nexusInstance: NexusInstanceReference,
    token: String,
    serviceClient: ServiceClient = EditorClient,
    clientExtensionId: Option[String] = None
  )(implicit ec: ExecutionContext): Future[Either[WSResponse, NexusInstance]] = {
    val params = clientExtensionId.map("clientIdExtension" -> _).getOrElse("" -> "")
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusInstance.toString}")
      .withHttpHeaders(AUTHORIZATION -> token, "client" -> serviceClient.client)
      .addQueryStringParameters(params)
      .get()
      .map { res =>
        res.status match {
          case OK =>
            Right(NexusInstance(Some(nexusInstance.id), nexusInstance.nexusPath, res.json.as[JsObject]))
          case _ => Left(res)
        }
      }
  }

  def put(
    wSClient: WSClient,
    apiBaseEndpoint: String,
    nexusInstance: NexusInstanceReference,
    editorInstance: EditorInstance,
    token: String,
    userId: String,
    serviceClient: ServiceClient = EditorClient
  )(implicit ec: ExecutionContext): Future[Either[WSResponse, Unit]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusInstance.toString}")
      .addHttpHeaders(AUTHORIZATION -> token, "client" -> serviceClient.client)
      .addQueryStringParameters("clientIdExtension" -> userId)
      .put(editorInstance.nexusInstance.content)
      .map { res =>
        res.status match {
          case OK | CREATED => Right(())
          case _            => Left(res)
        }
      }
  }

  def delete(
    wSClient: WSClient,
    apiBaseEndpoint: String,
    nexusInstance: NexusInstanceReference,
    token: String,
    serviceClient: ServiceClient = EditorClient
  )(implicit ec: ExecutionContext): Future[Either[WSResponse, Unit]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusInstance.toString}")
      .withHttpHeaders(AUTHORIZATION -> token, "client" -> serviceClient.client)
      .delete()
      .map { res =>
        res.status match {
          case OK | NO_CONTENT => Right(())
          case _               => Left(res)
        }
      }
  }

  def post(
    wSClient: WSClient,
    apiBaseEndpoint: String,
    nexusInstance: NexusInstance,
    user: Option[User],
    token: String,
    serviceClient: ServiceClient = EditorClient
  )(implicit ec: ExecutionContext): Future[Either[WSResponse, NexusInstanceReference]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusInstance.nexusPath.toString()}")
      .withHttpHeaders(AUTHORIZATION -> token, "client" -> serviceClient.client)
      .addQueryStringParameters("clientIdExtension" -> user.map(_.id).getOrElse(""))
      .post(nexusInstance.content)
      .map { res =>
        res.status match {
          case OK | CREATED =>
            Right(NexusInstanceReference.fromUrl((res.json \ EditorConstants.RELATIVEURL).as[String]))
          case _ => Left(res)
        }
      }
  }

  def getLinkingInstance(
    wSClient: WSClient,
    apiBaseEndpoint: String,
    from: NexusInstanceReference,
    to: NexusInstanceReference,
    linkingInstancePath: NexusPath,
    token: String,
    serviceClient: ServiceClient = EditorClient
  )(implicit ec: ExecutionContext): Future[Either[WSResponse, List[NexusInstanceReference]]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${from.toString}/links/${to.toString}/${linkingInstancePath.toString()}")
      .addHttpHeaders(AUTHORIZATION -> token, "client" -> serviceClient.client)
      .get()
      .map { res =>
        res.status match {
          case OK => Right(res.json.as[List[NexusInstanceReference]])
          case _  => Left(res)
        }
      }
  }
}
