package controllers.swagger

import com.google.inject.Inject
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.cache.Cached
import play.api.mvc._
import play.api.routing.Router

class ApiSpecs @Inject() (cc: ControllerComponents, cached: Cached) extends AbstractController(cc) {
  implicit val cl = getClass.getClassLoader

  val domainPackage = "models"

  private lazy val generator = SwaggerSpecGenerator(false, domainPackage)

  lazy val swagger = Action { _ =>
    generator.generate().fold(
      e => InternalServerError(s"Couldn't generate swagger: ${e.getMessage}"),
      s => Ok(s))
  }

  def specs = swagger
}