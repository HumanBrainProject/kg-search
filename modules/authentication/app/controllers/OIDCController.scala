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

package controllers

import com.google.inject.Inject
import javax.inject.Singleton
import models.AuthenticatedUserAction
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.{ConfigurationService, OIDCAuthService}

@Singleton
class OIDCController @Inject()(
  cc: ControllerComponents,
  authService: OIDCAuthService,
  authenticatedUserAction: AuthenticatedUserAction,
  config: ConfigurationService
) extends AbstractController(cc) {
  implicit val s = monix.execution.Scheduler.Implicits.global
  val logger = Logger(this.getClass)

  /**
    * Fetch the accesbible ES index for a specific user
    * @return A list of accessible ES index
    */
  def groups(): Action[AnyContent] = Action.async { implicit request =>
    authService
      .getUserInfo(request.headers)
      .flatMap { userinfo =>
        logger.debug(s"Authenticated user ${userinfo}")
        authService.groups(userinfo).map(l => Ok(Json.toJson(l)))
      }
      .runToFuture
  }

  def groupsOptions(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("").withHeaders("Allow" -> "GET, OPTIONS")
  }
}
