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

import com.google.inject.Inject
import helpers.{IDMHelper, NexusSpaceHandler}
import monix.eval.Task
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.{EmptyBody, WSClient, WSResponse}

class NexusSpaceService @Inject()(wSClient: WSClient) {
  val logger = Logger(this.getClass)

  def createGroups(
    nexusGroupName: String,
    adminGroupName: String,
    groupDescription: String,
    token: String,
    endpoint: String
  ): Task[WSResponse] = {
    val payload = NexusSpaceHandler.createGroupPayload(nexusGroupName, groupDescription)
    val adminPayload = NexusSpaceHandler.createGroupPayload(adminGroupName, groupDescription)
    val response = for {
      readGroup <- Task.deferFuture(
        wSClient.url(endpoint + s"/group").addHttpHeaders("Authorization" -> token).post(payload)
      )
      adminGroup <- Task.deferFuture(
        wSClient.url(endpoint + s"/group").addHttpHeaders("Authorization" -> token).post(adminPayload)
      )
    } yield (readGroup, adminGroup)

    response.flatMap {
      case (readGroup, adminGroup) =>
        (readGroup.status, adminGroup.status) match {
          case (201, 201) =>
            logger.debug(s"Groups created: $nexusGroupName, $adminGroupName")
            grantAdminToGroup(nexusGroupName, adminGroupName, token, endpoint)
          case (201, _) => Task.pure(adminGroup)
          case (_, 201) => Task.pure(readGroup)
          case (_, _) =>
            logger.error(
              s"Could not create group: \n group response :${readGroup.statusText} ${readGroup.body} \n admin group response : ${adminGroup.statusText}${adminGroup.body}"
            )
            Task.pure(readGroup)
        }
    }
  }

  private def grantAdminToGroup(
    nexusGroupName: String,
    adminGroupName: String,
    token: String,
    endpoint: String
  ): Task[WSResponse] = {
    val adminEnpoint = endpoint + s"/group/$adminGroupName/admin-groups/${IDMHelper.nexusAdmin}"
    val groupEnpoint = endpoint + s"/group/$nexusGroupName/admin-groups/$adminGroupName"

    val response = for {
      readGroup <- Task.deferFuture(
        wSClient.url(groupEnpoint).addHttpHeaders("Authorization" -> token, "Content-length" -> "0").post(EmptyBody)
      )
      adminGroup <- Task.deferFuture(
        wSClient.url(adminEnpoint).addHttpHeaders("Authorization" -> token, "Content-length" -> "0").post(EmptyBody)
      )
    } yield (readGroup, adminGroup)

    response.map {
      case (readGroup, adminGroup) =>
        (readGroup.status, adminGroup.status) match {
          case (201, 201) => readGroup
          case (201, _)   => adminGroup
          case (_, 201)   => readGroup
          case (_, _) =>
            logger.error(
              s"Could not assign group as admin: \n group response :${readGroup.body} \n admin group response : ${adminGroup.body}"
            )
            readGroup

        }
    }
  }

  def createNexusOrg(groupName: String, token: String, nexusEndpoint: String): Task[WSResponse] = {
    val payload = Json.obj(
      "@context" -> Json.obj(
        "schema" -> "http://schema.org"
      ),
      "schema:name" -> groupName
    )
    Task.deferFuture(
      wSClient
        .url(nexusEndpoint + s"/v0/organizations/$groupName")
        .addHttpHeaders("Authorization" -> token)
        .put(payload)
    )
  }

  def grantIAMrights(groupName: String, token: String, iamEndpoint: String): Task[WSResponse] = {
    val endpoint = iamEndpoint + s"/v0/acls/kg/$groupName"
    val payload = NexusSpaceHandler.aclPayload(
      Seq(groupName, s"${groupName}-admin"),
      Seq(Seq("read"), Seq("read", "write", "own", "publish"))
    )
    Task.deferFuture(wSClient.url(endpoint).addHttpHeaders("Authorization" -> token).put(payload))
  }

  def deleteOrg(groupName: String, token: String, nexusEndpoint: String): Task[WSResponse] = {
    Task.deferFuture(
      wSClient
        .url(nexusEndpoint + s"/v0/organizations/$groupName?rev=1")
        .addHttpHeaders("Authorization" -> token)
        .delete()
    )
  }

  def deleteGroup(groupName: String, token: String, apiEndpoint: String): Task[WSResponse] = {
    Task.deferFuture(wSClient.url(apiEndpoint + s"/group/$groupName").addHttpHeaders("Authorization" -> token).delete())
  }

  def removeACLS(groupName: String, token: String, iamEndpoint: String): Task[WSResponse] = {
    Task.deferFuture(
      wSClient.url(iamEndpoint + s"/v0/acls/kg/$groupName").addHttpHeaders("Authorization" -> token).delete()
    )
  }
}
