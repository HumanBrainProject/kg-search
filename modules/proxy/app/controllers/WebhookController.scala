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
package controllers

import akka.util.ByteString
import com.google.inject.Inject
import play.api.http.HttpEntity
import play.api.mvc._
import services.GithubHookService

import scala.concurrent.{ExecutionContext, Future}

class WebhookController @Inject()(
  cc: ControllerComponents,
  githubHookService: GithubHookService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  def github(gitlabProjectId: String, token: String): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        githubHookService
          .postToGitlab(json, gitlabProjectId, token)
          .map { res =>
            Result(
              header = ResponseHeader(
                res.status,
                res.headers.map(s => s._1 -> s._2.mkString(" "))
              ),
              body = HttpEntity.Strict(ByteString(res.body), None)
            )
          }
      case None =>
        Future {
          BadRequest("Missing payload")
        }
    }

  }
}
