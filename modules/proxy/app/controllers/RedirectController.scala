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
import play.api.http.HeaderNames.USER_AGENT
import play.api.http.HttpEntity
import play.api.mvc._
import services.{ConfigurationService, RedirectService}

import scala.concurrent.{ExecutionContext, Future}

class RedirectController @Inject()(
  cc: ControllerComponents,
  redirectService: RedirectService,
  config: ConfigurationService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  /**
    *  if google bot generate static html from es json, else redirect to dynamic url
    * @param dataType The type of data requested
    * @param id The id of the instance
    * @return A redirect to the dynamic page or a html page for the google bot
    */
  def get(dataType: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    request.headers.toSimpleMap.get(USER_AGENT) match {
      case Some(u) if u.toLowerCase == "googlebot" =>
        redirectService.renderHtml(dataType, id).map {
          case Right(html) => Ok(html)
          case Left(res) =>
            Result(
              header = ResponseHeader(
                res.status,
                res.headers.map(s => s._1 -> s._2.mkString(" "))
              ),
              body = HttpEntity.Strict(ByteString(res.body), None)
            )
        }
      case _ => Future(TemporaryRedirect(s"${config.hbpUrl}/webapp/#$dataType/$id"))
    }
  }
}
