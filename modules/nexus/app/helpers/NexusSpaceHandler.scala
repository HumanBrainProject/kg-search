
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

package helpers

import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.libs.ws.{EmptyBody, WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

object NexusSpaceHandler {
  def aclPayload(groupsName: Seq[String], groupsGrants: Seq[Seq[String]]): JsObject = {
    val aclsContent = JsArray(
      groupsName.zip(groupsGrants).map {
        case (groupName, groupGrants) =>
          Json.obj(
            "identity" -> Json.obj(
              "realm" -> "HBP",
              "group" -> s"nexus-$groupName",
              "@type" -> "GroupRef"
            ),
            "permissions" -> JsArray(
              groupGrants.map(JsString(_))
            )
          )
      }
    )
    Json.obj(
      "acl" -> aclsContent
    )
  }

  def createGroupPayload(nexusGroupName: String, groupDescription: String): JsObject = {
    Json.obj(
      "description" -> groupDescription,
      "name" -> nexusGroupName
    )
  }
}