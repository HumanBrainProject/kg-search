package helpers

import common.helpers.ESHelper
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
  override def fakeApplication() = GuiceApplicationBuilder().configure("play.http.filters" -> "play.api.http.NoHttpFilters").build()

  "ESHelper#filterNexusGroup" should {

    "return groups without prefix and suffix" in {
      val origin = List("hbp-group1-admin", "nexus-group2", "other-group3")
      val expected = List("nexus-group2")
      val res = ESHelper.filterNexusGroups(origin, s => s)
      assert(res == expected)
    }

  }
}
