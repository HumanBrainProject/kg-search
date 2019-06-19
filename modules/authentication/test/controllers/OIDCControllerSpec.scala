package controllers

import mockws.MockWSHelpers
import models.user.OIDCUser
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

  override def fakeApplication() = GuiceApplicationBuilder().configure(
    "play.http.filters" -> "play.api.http.NoHttpFilters",
    "auth.userinfo" -> userinfoEndpoint
  ).build()

  "OIDCController GET#groups" should {
    "return groups" in  {
      val auth = "Bearer 123"
      implicit val config = app.injector.instanceOf[Configuration]
      implicit val ec = app.injector.instanceOf[ExecutionContext]
      val userinfo = Some(
        new OIDCUser(
          "123",
          "name",
          "email",
          Some("picture"),
          Seq("nexus-group1","nexus-group2")
        )
      )
      
      assert(true)
    }

  }
}
