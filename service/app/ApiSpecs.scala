package controllers.swagger

import com.google.inject.Inject
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.Logger
import play.api.cache.Cached
import play.api.mvc._
import play.api.routing.Router

class ApiSpecs @Inject() (cc: ControllerComponents, cached: Cached) extends AbstractController(cc) {
  implicit val cl = getClass.getClassLoader
  val logger = Logger(this.getClass)
  val domainPackage = "models"

  private lazy val generator = SwaggerSpecGenerator(false, domainPackage)

  lazy val swagger = Action { _ =>
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

  def specs = swagger
}