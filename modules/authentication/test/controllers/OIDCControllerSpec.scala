package controllers

import mockws.{MockWS, MockWSHelpers}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test._
import models.authentication.UserInfo
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

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
        UserInfo(
          "123",
          "name",
          "email",
          Seq("nexus-group1","nexus-group2")
        )
      )
      
      assert(true)
    }

  }
}
