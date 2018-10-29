
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
package editor.services

import authentication.service.OIDCAuthService
import common.helpers.ConfigMock
import common.helpers.ConfigMock._
import common.models.{Favorite, FavoriteGroup, NexusUser}
import common.services.ConfigurationService
import editor.models.EditorUser
import mockws.{MockWS, MockWSHelpers}
import nexus.services.NexusService
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.test.Helpers.POST
import play.api.test.Injecting

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class EditorUserServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with MockitoSugar with Injecting {

  override def fakeApplication(): Application = ConfigMock.fakeApplicationConfig.build()

  "EditorUserService#getUser" should{
    "return an editor user" in {
      val fakeEndpoint = s"${kgQueryEndpoint}/query/"

      val id = "1"
      val idUser = "nexusUUID1"
      val nexusIdUser = s"http://nexus.com/v0/data/$idUser"
      val nexusUser = NexusUser(
        nexusIdUser,
        "",
        "",
        Seq(),
        Seq()
      )
      val user = EditorUser(nexusIdUser, nexusUser)
      val endpointResponse = Json.parse(
        s"""
          |{
          |    "nexusId": "$nexusIdUser",
          |    "userId": "$id"
          |}
        """.stripMargin
      )
      implicit val ws = MockWS {
        case (POST, fakeEndpoint) => Action {
          Ok(Json.obj("results" -> Json.toJson(List(endpointResponse))))
        }
      }
      val ec = global
      val oidcService = mock[OIDCAuthService]
      val nexusService = mock[NexusService]
      val configService = mock[ConfigurationService]
      val service = new EditorUserService(configService, ws,nexusService,oidcService)(ec)

      val res = Await.result(service.getUser(nexusUser), FiniteDuration(10 ,"s"))

      res.isDefined mustBe true
      res.get mustBe user
    }
  }


}
