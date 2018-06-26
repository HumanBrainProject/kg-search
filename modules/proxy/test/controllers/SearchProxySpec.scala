package controllers

import akka.stream.Materializer
import mockws.MockWS
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Results.Ok
import play.api.test._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class SearchProxySpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  val userinfoEndpoint: String = "http://www.userinfo.com"

  override def fakeApplication() = GuiceApplicationBuilder().configure(
    "play.http.filters" -> "play.api.http.NoHttpFilters",
    "oidc.userinfo" -> userinfoEndpoint,
    "es.host" -> "/eshost"
  ).build()
  implicit lazy val materializer: Materializer = app.materializer

  "SearchProxy" should {
    "Get ES Indices as list" in {
      assert(true)
    }
  }


}
