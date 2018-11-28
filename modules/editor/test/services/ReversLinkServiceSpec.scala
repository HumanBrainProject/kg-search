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

import constants.EditorConstants.UPDATE
import mockws.MockWSHelpers
import models._
import models.instance.{EditorInstance, NexusInstance}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json

class ReversLinkServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with MockitoSugar{


  "getReverseLinks" should {
    "return an editor instance with a link to the main instance" in {
      val path = NexusPath("org", "domain", "schema", "version")
      val instanceId = "123"
      val doiPath = NexusPath("datacite/core/doi/v0.0.4")
      val targetField = "https://schema.hbp.eu/reference"
      val baseUrl = "http://nexus-dev.hbp.com"
      object reverseLinkService extends ReverseLinkService
      val formRegistry = FormRegistry(
        Map(
          path -> UISpec(
            "Activity", Map(
              "http://schema.org/name" -> EditorFieldSpecification(
                "Name", None, InputText, None, None, None, None, None
              ),
              "http://hbp.eu/minds#doi" -> EditorFieldSpecification(
                "Methods", Some("datacite/core/doi/v0.0.4"), DropdownSelect,
                None, Some("id"), Some("label"), Some(true), Some(true), Some(true), Some(targetField)
              )
            ), Some(UIInfo(
              "http://schema.org/name", List("http://schema.org/name",
                "http://schema.org/description"), None
            ))
          )

        )
      )
      val doiId = "456"
      val originalInstance = NexusInstance(
        Some(instanceId),
        path,
        Json.obj(
          "http://schema.org/name" -> "name"
        )
      )

      val update = EditorInstance(
        NexusInstance(
          Some(instanceId),
          path,
          Json.obj(
            "http://schema.org/name" -> "name",
            "http://hbp.eu/minds#doi"-> Json.obj(
              "@id" -> s"$baseUrl/v0/data/${doiPath.toString()}/$doiId"
            )
          )
        )
      )

      val expected = EditorInstance(
        NexusInstance(
          Some(doiId),
          doiPath,
          Json.obj(
            targetField-> Json.obj(
              "@id" -> s"$baseUrl/v0/data/${path.toString()}/$instanceId"
            )
          )
        )
      )

      val res = reverseLinkService.getReverseLinks(update, formRegistry,originalInstance,  baseUrl)

      res._1 mustBe EditorInstance(originalInstance)
      res._2 mustBe List((UPDATE, expected, targetField))

    }
  }

}
