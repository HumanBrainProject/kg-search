/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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

package controllers.swagger

import com.google.inject.Inject
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.Logger
import play.api.cache.Cached
import play.api.mvc._

class ApiSpecs @Inject()(cc: ControllerComponents, cached: Cached) extends AbstractController(cc) {
  implicit val cl: ClassLoader = getClass.getClassLoader
  val logger: Logger = Logger(this.getClass)
  val domainPackage = "models"

  private lazy val generator = SwaggerSpecGenerator(false, domainPackage)

  lazy val swagger: Action[AnyContent] = Action { _ =>
    generator.generate().fold(
      e => {
        logger.error(s"Couldn't generate swagger: ${e.getMessage}")
        InternalServerError(s"Couldn't generate swagger: ${e.getMessage}")
      },
      s => {
        logger.info(s"Swagger file generated")
        Ok(s)
      }
    )
  }

  def specs:Action[AnyContent] = swagger
}