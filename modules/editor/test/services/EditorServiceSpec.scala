
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

import helpers.ConfigMock
import mockws.MockWSHelpers
import models.{FormRegistry, NexusPath}
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference}
import models.user.NexusUser
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.Injecting

import scala.concurrent.ExecutionContext

class EditorServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with MockitoSugar with Injecting {

  override def fakeApplication(): Application = ConfigMock.fakeApplicationConfig.build()

  val formRegistry: FormRegistry = FormRegistry(
    Json.obj(
      "org" -> Json.obj(
        "domain" -> Json.obj(
          "schema" -> Json.obj(
            "v1" -> Json.obj(
              "fields" -> Json.obj(
                "name" -> Json.obj(
                  "type" -> "DropdownSelect",
                    "label" -> "Methods",
                    "instancesPath"-> "minds/experiment/method/v0.0.4",
                ),
                "activity" -> Json.obj(
                  "type" -> "DropdownSelect",
                  "label" -> "Methods",
                  "instancesPath"-> "minds/experiment/method/v0.0.4",
                ),
                "region" -> Json.obj(
                  "type" -> "DropdownSelect",
                  "label" -> "Methods",
                  "instancesPath"-> "minds/experiment/method/v0.0.4",
                ),
                "pla" -> Json.obj(
                  "type" -> "DropdownSelect",
                  "label" -> "Methods",
                  "instancesPath"-> "minds/experiment/method/v0.0.4",
                )
              )
            )
          )
        )
      )
    )
  )
  "Generate user update" should {
    "return a diff instance with required fields" in {

      implicit val ec = app.injector.instanceOf[ExecutionContext]
      val ws = mock[WSClient]
      val configService = mock[ConfigurationService]
      val editorService = new EditorService(ws, configService)
      val path = NexusPath("org", "domain", "schema", "v1")
      val id = "000"
      val ref = NexusInstanceReference(path, id)
      val dummyUser = NexusUser(
        "1",
        "Joe",
        "email",
        Seq(),
        Seq()
      )

      val currentInstance = NexusInstance(
        Some(id),
        path,
        Json.obj(
          "name" -> "Test",
          "activity" -> Json.arr(
            Json.obj(
              "id" -> "123"
            ),
            Json.obj(
              "id" -> "456"
            )
          ),
          "region" -> "1"
        )
      )

      val update = NexusInstance(
        Some(id),
        path,
        Json.obj(
          "name" -> "New Name",
          "activity" -> Json.arr(
            Json.obj(
              "id" -> "123"
            )
          ),
          "pla" -> "test",
          "region" -> "1"
        )
      )

      val expected = EditorInstance(
        NexusInstance(
          Some(id),
          path,
          Json.obj(
            "name" -> "New Name",
            "activity" -> Json.arr(
              Json.obj(
                "id" -> "123"
              )
            ),
            "pla" -> "test"
          )
        )
      )

      val result = editorService.generateUpdateFromUser(
        ref,
        currentInstance,
        update,
        dummyUser,
        formRegistry,
        "123"
      )

      result mustBe expected

    }
  }

}
