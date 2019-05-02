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

import com.google.inject.Inject
import play.api.mvc._
import services.{ConfigurationService, RedirectService}

import scala.concurrent.ExecutionContext

class RedirectController @Inject()(
  cc: ControllerComponents,
  redirectService: RedirectService,
  config: ConfigurationService
)(
  implicit ec: ExecutionContext
) extends AbstractController(cc) {

  /**
    *  redirect to dynamic url
    * @param dataType The type of data requested
    * @param id The id of the instance
    * @param group The group of data requested
    * @return A redirect to the dynamic page
    */
  def get(dataType: String, id: String, group: Option[String]): Action[AnyContent] = Action { implicit request =>
    group match {
      case Some(name) =>
        Redirect(
          s"${config.hbpUrl}/?${RedirectController.groupKeyQueryString}=$name&${RedirectController.searchFalseQueryString}#$dataType/$id"
        )
      case None =>
        Redirect(
          s"${config.hbpUrl}/?${RedirectController.searchFalseQueryString}#$dataType/$id"
        )
    }
  }

  /**
    *  redirect to dynamic url
    * @param org The org of data requested
    * @param domain The domain of data requested
    * @param schema The schema of data requested
    * @param version The version of data requested
    * @param id The id of the instance
    * @return A redirect to the dynamic page
    */
  def getFullPath(org: String, domain: String, schema: String, version: String, id: String): Action[AnyContent] =
    Action { implicit request =>
      Redirect(
        s"${config.hbpUrl}/?#$org/$domain/$schema/$version/$id"
      )
    }
}

object RedirectController {
  val searchFalseQueryString: String = "search=false"
  val groupKeyQueryString: String = "group"
}
