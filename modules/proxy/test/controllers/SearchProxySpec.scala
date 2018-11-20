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
