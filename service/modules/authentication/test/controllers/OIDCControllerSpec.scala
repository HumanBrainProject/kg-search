package controllers

import mockws.MockWSHelpers
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._

import scala.concurrent.ExecutionContext

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class OIDCControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with Injecting {
  val userinfoEndpoint: String = "http://www.userinfo.com"

  override def fakeApplication() =
    GuiceApplicationBuilder()
      .configure(
        "play.http.filters" -> "play.api.http.NoHttpFilters",
        "auth.userinfo"     -> userinfoEndpoint
      )
      .build()

}
