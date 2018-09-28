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
package editor.helpers

import common.models.{NexusInstance, NexusPath}
import editor.helpers.InstanceHelper
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsNull, Json}

class InstanceHelperSpec extends PlaySpec with GuiceOneAppPerSuite {
  val id = "123"
  val reconId = "321"
  val path = NexusPath("org", "domain", "schema", "version")

  "BuildDiffEntity" should {
    "correctly build a diff when a field is modified" in {

      val content = Json.obj(
        "name" -> "test"
      )
      val original = NexusInstance(
        Some(id), path, content
      )

      val newInstance = NexusInstance(
        None, path, Json.obj(
          "name" -> "new name"
        )
      )

      val res = InstanceHelper.buildDiffEntity(original, newInstance, original)

      res mustBe Json.obj(
        "name" -> "new name"
      )
    }
    "correctly build a diff when a field is added" in {
      val content = Json.obj(
        "name" -> "test",
        "description" -> "description"
      )
      val original = NexusInstance(
        Some(id), path, Json.obj(
          "name" -> "test"
        )
      )

      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId), path, Json.obj(
          "name" -> "test"
        )
      )

      val newInstance = NexusInstance(
        None, path, content
      )

      val res = InstanceHelper.buildDiffEntity(currentlyDisplayedInstance, newInstance, original)

      res mustBe Json.obj(
        "description" -> "description"
      )
    }
    "correctly build a diff when a field is removed" in {
      val original = NexusInstance(
        Some(id), path, Json.obj(
          "name" -> "test",
          "description" -> "description"
        )
      )
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId), path, Json.obj(
          "name" -> "test",
          "description" -> "description"

        )
      )

      val newInstance = NexusInstance(
        None, path, Json.obj(
          "name" -> "test"
        )
      )

      val res = InstanceHelper.buildDiffEntity(currentlyDisplayedInstance, newInstance, original)

      res mustBe Json.obj(
        "description" -> JsNull
      )
    }
    "correctly manage arrays by returning the final array not its difference" in {
      val original = NexusInstance(
        Some(id), path, Json.obj(
          "name" -> "test",
          "description" -> "description"
        )
      )
      val currentlyDisplayedInstance = NexusInstance(
        Some(reconId), path, Json.obj(
          "name" -> "test",
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
        None, path, Json.obj(
          "name" -> "test",
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

      val res = InstanceHelper.buildDiffEntity(currentlyDisplayedInstance, newInstance, original)

      res mustBe Json.obj(
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
    }
  }

}
