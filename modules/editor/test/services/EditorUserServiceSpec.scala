package editor.services

import authentication.service.OIDCAuthService
import common.helpers.ConfigMock
import common.helpers.ConfigMock._
import common.models.{EditorUser, Favorite, FavoriteGroup}
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
      val nexusIdUser = "nexusUUID1"
      val nexusIdFavGroup = "nexusUUID2"
      val nexusIdFav = "nexusUUID3"
      val name = "Group"
      val instanceId = "minds/core/dataset/v0.0.4/123"
      val fav = Favorite(nexusIdFav, instanceId)
      val favGroup = FavoriteGroup(nexusIdFavGroup, name, Seq(fav))
      val user = EditorUser(nexusIdUser, id,  Seq(favGroup))
      val endpointResponse = Json.parse(
        s"""
          |{
          |    "nexusId": "$nexusIdUser",
          |    "id": "1",
          |    "favoriteGroups": [
          |        {
          |            "nexusId": "$nexusIdFavGroup",
          |            "name": "$name",
          |            "favorites": [
          |                {
          |                    "nexusId": "$nexusIdFav",
          |                    "instance": { "@id": "$instanceId"}
          |                }
          |            ]
          |        }
          |    ]
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
      val service = new EditorUserService(fakeApplication().configuration, ws,nexusService,oidcService)(ec)

      val res = Await.result(service.getUser(id), FiniteDuration(10 ,"s"))

      res.isDefined mustBe true
      res.get mustBe user
    }
  }


}
