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

import models.NexusPath
import models.instance.{EditorInstance, NexusInstance}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsNull, Json}

class InstanceOpSpec extends PlaySpec with GuiceOneAppPerSuite {
  val id = "123"
  val reconId = "321"
  val path = NexusPath("org", "domain", "schema", "version")

  "BuildDiffEntity" should {
    "correctly build a diff when a field is modified" in {

      val content = Json.obj(
        "name" -> "test"
      )
      val original = NexusInstance(
        Some(id),
        path,
        content
      )

      val newInstance = NexusInstance(
        None,
        path,
        Json.obj(
          "name" -> "new name"
        )
      )

      val expected = EditorInstance(
        NexusInstance(
          Some(id),
          path,
          Json.obj(
            "name" -> "new name"
          )
        )
      )
      val res = InstanceOp.buildDiffEntity(original, newInstance)

      res mustBe expected
    }
    "correctly build a diff when a field is added" in {
      val content = Json.obj(
        "name"        -> "test",
        "description" -> "description"
      )

      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId),
        path,
        Json.obj(
          "name" -> "test"
        )
      )

      val newInstance = NexusInstance(
        None,
        path,
        content
      )

      val res = InstanceOp.buildDiffEntity(currentlyDisplayedInstance, newInstance)

      val expected = EditorInstance(
        NexusInstance(
          Some(reconId),
          path,
          Json.obj(
            "description" -> "description"
          )
        )
      )

      res mustBe expected
    }
    "correctly build a diff when a field is removed" in {
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId),
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description"
        )
      )

      val newInstance = NexusInstance(
        None,
        path,
        Json.obj(
          "name" -> "test"
        )
      )

      val res = InstanceOp.buildDiffEntity(currentlyDisplayedInstance, newInstance)

      val expected = EditorInstance(
        NexusInstance(
          Some(reconId),
          path,
          Json.obj(
            "description" -> JsNull
          )
        )
      )

      res mustBe expected
    }
    "correctly manage arrays by returning the final array not its difference" in {
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId),
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities" -> Json.toJson(
            List(
              Json.obj(
                "id" -> "activity1"
              ),
              Json.obj(
                "id" -> "activity2"
              ),
              Json.obj(
                "id" -> "activity3"
              )
            )
          )
        )
      )

      val newInstance = NexusInstance(
        None,
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities" -> Json.toJson(
            List(
              Json.obj(
                "id" -> "activity1"
              ),
              Json.obj(
                "id" -> "activity3"
              )
            )
          )
        )
      )

      val res = InstanceOp.buildDiffEntity(currentlyDisplayedInstance, newInstance)

      val expected = EditorInstance(
        NexusInstance(
          Some(reconId),
          path,
          Json.obj(
            "activities" -> Json.toJson(
              List(
                Json.obj(
                  "id" -> "activity1"
                ),
                Json.obj(
                  "id" -> "activity3"
                )
              )
            )
          )
        )
      )

      res mustBe expected
    }
    "reflect correct array changes" in {
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId),
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities" -> Json.toJson(
            List(
              Json.obj(
                "id" -> "activity1"
              ),
              Json.obj(
                "id" -> "activity2"
              ),
              Json.obj(
                "id" -> "activity3"
              )
            )
          ),
          "contributors" -> Json.toJson(
            List(
              "bill",
              "james",
              "jane"
            )
          )
        )
      )

      val newInstance = NexusInstance(
        None,
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities"  -> JsArray(),
          "contributors" -> Json.toJson(
            "james",
            "jane"
          )
        )
      )

      val expected = EditorInstance(
        NexusInstance(
          Some(reconId),
          path,
          Json.obj(
            "activities" -> JsArray(),
            "contributors" -> Json.toJson(
              "james",
              "jane"
            )
          )
        )
      )

      val res = InstanceOp.buildDiffEntity(currentlyDisplayedInstance, newInstance)

      res mustBe expected
    }

    "remove a field in the diff value if the array was not changed" in {
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId),
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities"  -> JsArray(),
          "contributors" -> Json.toJson(
            List(
              "james",
              "jane"
            )
          )
        )
      )

      val newInstance = NexusInstance(
        None,
        path,
        Json.obj(
          "name"        -> "test",
          "description" -> "description",
          "activities"  -> JsArray(),
          "contributors" -> Json.toJson(
            "james",
            "jane"
          )
        )
      )

      val expected = EditorInstance(
        NexusInstance(
          Some(reconId),
          path,
          Json.obj()
        )
      )

      val res = InstanceOp.buildDiffEntity(currentlyDisplayedInstance, newInstance)

      res mustBe expected
    }
  }

  "Remove Internal field" should {
    "not consider an array of single object or this unique object" in {
      val path = NexusPath("org", "dom", "schema", "version")
      val instance = NexusInstance(
        None,
        path,
        Json.obj(
          "array" -> Json.toJson(List(Json.obj("@id" -> "test")))
        )
      )

      val update = EditorInstance(
        NexusInstance(
          Some("123"),
          path,
          Json.obj("array" -> Json.obj("@id" -> "test"))
        )
      )

      val res = InstanceOp.removeEmptyFieldsNotInOriginal(instance, update)

      res.nexusInstance.content mustBe Json.obj()
    }
    "work both ways" in {
      val path = NexusPath("org", "dom", "schema", "version")
      val instance = NexusInstance(None, path, Json.obj("array" -> Json.obj("@id" -> "test")))

      val update = EditorInstance(
        NexusInstance(
          Some("123"),
          path,
          Json.obj("array" -> Json.toJson(List(Json.obj("@id" -> "test"))))
        )
      )
      val res = InstanceOp.removeEmptyFieldsNotInOriginal(instance, update)
      res.nexusInstance.content mustBe Json.obj()
    }
  }

}
