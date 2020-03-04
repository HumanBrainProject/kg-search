/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class GithubHookService @Inject()(WSClient: WSClient)(implicit executionContext: ExecutionContext) {

  def postToGitlab(json: JsValue, projectID: String, token: String): Future[WSResponse] = {
    val payload = GitLabCommitPayload(json.as[JsObject].value, token)
    WSClient
      .url(s"https://gitlab.humanbrainproject.org/api/v4/projects/$projectID/trigger/pipeline")
      .post(Json.toJson(payload)(GitLabCommitPayload.writes))
  }
}

case class GitLabCommitPayload(
  token: String,
  ref: String,
  commit_ref: String,
  commit: String,
  commit_msg: String
)

object GitLabCommitPayload {

  def apply(m: scala.collection.Map[String, JsValue], token: String): GitLabCommitPayload = new GitLabCommitPayload(
    token,
    "master",
    m("ref").as[String],
    m("after").as[String],
    (m("head_commit") \ "message").as[String]
  )

  implicit val writes = new Writes[GitLabCommitPayload] {
    override def writes(o: GitLabCommitPayload): JsValue = Json.obj(
      "token" -> o.token,
      "ref"   -> o.ref,
      "variables" -> Json.obj(
        "commit_ref" -> o.commit_ref,
        "commit"     -> o.commit,
        "commit_msg" -> o.commit_msg
      )
    )
  }
}
