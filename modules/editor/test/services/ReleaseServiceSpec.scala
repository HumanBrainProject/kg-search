package services

import editor.services.ReleaseService
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.components.OneAppPerSuiteWithComponents
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import org.scalatest._
import Matchers._


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
