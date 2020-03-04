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
package helpers

import models.user.Group
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Injecting

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class ESHelperSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  override def fakeApplication() =
    GuiceApplicationBuilder().configure("play.http.filters" -> "play.api.http.NoHttpFilters").build()

  "ESHelper#filterNexusGroup" should {

    "return groups without prefix and suffix" in {
      val origin = List(Group("hbp-group1-admin"), Group("nexus-group2"), Group("other-group3"))
      val expected = List("nexus-group2")
      val res = ESHelper.filterNexusGroups(origin, s => s)
      assert(res == expected)
    }

  }
}
