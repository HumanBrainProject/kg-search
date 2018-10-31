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
package services

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._


class ReleaseServiceSpec extends  PlaySpec with GuiceOneAppPerSuite {


  "ReleaseService#spec" should {
    "Handle empty arrays of children" in {
      val input = Json.obj(
        "http://schema.org/name" -> "test",
        "@type" -> "Test",
        "children" -> Json.arr(
          Json.obj("@id" -> "test/123", "linkType" -> "core-child/123", "http://schema.org/name" -> "first child", "@type" -> "Test"),
          Json.obj("@id" -> "test/456", "linkType" -> "core-child/456", "http://schema.org/name" -> "second child", "@type" -> "Test"),
          Json.obj("@id" -> "test/789", "linkType" -> "core-child/789", "http://schema.org/name" -> "third child",
            "children" -> Json.arr(), "@type" -> "Test"
          )
        )
      )

      val expected = Map[String, JsValue](
        "label" -> JsString("test"),
        "type" -> JsString("Test"),
        "children" -> Json.arr(
          Json.obj("@id" -> "test/123","linkType" -> Json.arr("child"), "label" -> "first child", "type" -> "Test"),
          Json.obj("@id" -> "test/456","linkType" -> Json.arr("child"), "label" -> "second child", "type" -> "Test"),
          Json.obj("@id" -> "test/789","linkType" -> Json.arr("child"), "label" -> "third child",
            "children" -> Json.arr(), "type" -> JsString("Test")
          )
        )
      )
      val res = ReleaseService.recSpec(input).as[JsObject].value

      res("children").as[List[JsValue]] should contain theSameElementsAs expected("children").as[List[JsValue]]

    }
    "Handle null values" in {
      val input = Json.obj(
        "http://schema.org/name" -> "test",
        "@type" -> "Test",
        "children" -> Json.arr(
          Json.obj("@id" -> "test/123", "linkType" -> "core-child/123", "http://schema.org/name" -> "first child", "@type" -> "Test"),
          Json.obj("@id" -> "test/456", "linkType" -> "core-child/456", "http://schema.org/name" -> "second child",
            "children" -> Json.arr(JsNull, JsNull), "@type" -> "Test"
          ),
          JsNull
        )
      )

      val expected = Map(
        "label" -> JsString("test"),
        "type" -> JsString("Test"),
        "children" -> Json.arr(
          Json.obj("@id" -> "test/123","linkType" -> Json.arr("child"), "label" -> "first child", "type" -> "Test"),
          Json.obj("@id" -> "test/456","linkType" -> Json.arr("child"), "label" -> "second child",
            "children" -> Json.arr(), "type" -> JsString("Test")
          )
        )
      )
      val res = ReleaseService.recSpec(input).as[JsObject].value

      res.size shouldBe expected.size

      res("children").as[List[JsValue]] should contain theSameElementsAs expected("children").as[List[JsValue]]
    }
  }

}
