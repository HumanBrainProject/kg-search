
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

import helpers.NavigationHelper
import models.{FormRegistry, NexusPath}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import services.FormService

class NavigationHeperSpec  extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar{
  val formService = mock[FormService]
  val registry = Json.parse(
    """
      |{
      | "minds":{
      |   "core":{
      |     "dataset":{
      |       "v0.0.4":{
      |
      |       }
      |     }
      |   }
      | }
      |}
    """.stripMargin).as[JsObject]

  "NavigationHelper#generateBackLink" should{
    "return a correctly formatted back link" in {
      val path = NexusPath("minds", "core", "dataset", "v0.0.4")
      val expected = "minds/core/dataset"
      when(formService.formRegistry).thenReturn(FormRegistry(registry))
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled", formService))
    }
    "return an empty string if the path is not valid" in {
      val path = NexusPath("this", "doesnot", "exists", "v0.0.4")
      val expected = ""
      val formService = mock[FormService]
      when(formService.formRegistry).thenReturn(FormRegistry(registry))
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled", formService))
    }
    "return a path with an original organization" in {
      val path = NexusPath("mindsreconciled", "core", "dataset", "v0.0.4")
      val expected = "minds/core/dataset"
      val formService = mock[FormService]
      when(formService.formRegistry).thenReturn(FormRegistry(registry))
      assert(expected == NavigationHelper.generateBackLink(path, "reconciled", formService))
    }
  }

}
