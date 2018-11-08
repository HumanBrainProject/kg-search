
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
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._

trait InstanceApiService {
    val instanceEndpoint = "/api/instances"

    def get(
           wSClient: WSClient,
           apiBaseEndpoint: String,
           nexusInstance: NexusInstanceReference,
           token: String,
           serviceClient: ServiceClient = EditorClient
         )(implicit ec: ExecutionContext): Future[Either[WSResponse, NexusInstance]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusInstance.toString}")
      .withHttpHeaders("Authorization" -> token, "client" -> serviceClient.client)
      .get()
      .map { res =>
        res.status match {
          case OK => Right(NexusInstance(Some(nexusInstance.id), nexusInstance.nexusPath, res.json.as[JsObject]))
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
      .withHttpHeaders("Authorization" -> token, "client" -> serviceClient.client)
      .withQueryStringParameters("clientIdExtension" -> userId)
      .put(editorInstance.nexusInstance.content)
      .map { res =>
        res.status match {
          case OK | CREATED => Right(())
          case _ => Left(res)
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
      .withHttpHeaders("Authorization" -> token, "client" -> serviceClient.client)
      .delete()
      .map { res =>
        res.status match {
          case OK | NO_CONTENT => Right(())
          case _ => Left(res)
        }
      }
  }

  def post(
            wSClient: WSClient,
            apiBaseEndpoint: String,
            nexusInstance: NexusInstance,
            nexusPath: NexusPath,
            token: String,
            serviceClient: ServiceClient = EditorClient
          )(implicit ec: ExecutionContext): Future[Either[WSResponse, NexusInstance]] = {
    wSClient
      .url(s"$apiBaseEndpoint$instanceEndpoint/${nexusPath.toString}")
      .withHttpHeaders("Authorization" -> token, "client" -> serviceClient.client)
      .post(nexusInstance.content)
      .map { res =>
        res.status match {
          case OK | CREATED =>
            val ref = NexusInstanceReference.fromUrl((res.json \ EditorConstants.IDRESPONSEFIELD).as[String])
            Right(nexusInstance.copy(nexusUUID = Some(ref.id) ))
          case _ => Left(res)
        }
      }
  }


}
