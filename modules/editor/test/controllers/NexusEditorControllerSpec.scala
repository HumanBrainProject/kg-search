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
package editor.controllers

import common.helpers.ConfigMock
import common.helpers.ConfigMock._
import common.models.{NexusInstance, NexusPath, OIDCUser}
import mockws.{MockWS, MockWSHelpers}
import authentication.service.OIDCAuthService
import common.services.ConfigurationService
import editor.helpers.ReconciledInstanceHelper
import editor.models.{EditorInstance, ReconciledInstance}
import editor.services.{ArangoQueryService, EditorService, ReleaseService}
import nexus.services.NexusService
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future
import scala.reflect.macros.whitebox

class NexusEditorControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with MockitoSugar with Injecting {


  override def fakeApplication(): Application = ConfigMock.fakeApplicationConfig.build()

  "Retrieve from manual" should {
    "build the frequency of fields in manual" in {
      //      implicit val order = Ordering.fromLessThan[(JsValue, Int)]( _._2 > _._2)
      //      val manualUpdates: IndexedSeq[JsValue] = Json.arr(Json.obj("source" -> Json.obj("name" -> "Bill", "desc" -> "Bill is cool")), Json.obj("source" -> Json.obj( "name" -> "Billy", "desc" -> "Billy is cooler")), Json.obj("source" -> Json.obj( "name" -> "Bill", "desc" -> "Billy is cooler"))).value
      //      val expectedResult: Map[String, SortedSet[(JsValue, Int)]] = Map( "name" -> SortedSet(JsString("Billy").as[JsValue] -> 2 , JsString("Bill").as[JsValue] -> 1), "desc" -> SortedSet(JsString( "Billy is cooler").as[JsValue] -> 2, JsString("Bill is cool").as[JsValue] -> 1))
      //      val result = Instance.buildManualUpdatesFieldsFrequency(manualUpdates)
      //      val keyDiff = (expectedResult.keySet -- result.keySet) ++ (result.keySet -- expectedResult.keySet)
      //      assert(keyDiff.isEmpty)
      //      forAll(result.toList){
      //        el => expectedResult(el._1) should equal (el._2)
      //      }
    }
  }
  "InstanceController#list" should {
    "return a 200 with a correctly formatted result" in {
      val datatype = NexusPath("data/core/datatype/v0.0.4".split("/"))
      val nexusBase = "http://nexus.org/v0/data"
      import scala.concurrent.ExecutionContext.Implicits._
      val instances = Json.arr(
        Json.obj("id" -> s"${datatype.toString()}/123", "description"->"","label" -> "dataname1"),
        Json.obj("id" -> s"${datatype.toString()}/321", "description"->"","label" -> "dataname2")
      )
      val fakeEndpoint = s"$kgQueryEndpoint/arango/instances/${datatype.toString()}"

      val mockCC = stubControllerComponents()
      val ec = global
      val instanceService = mock[EditorService]

      val oidcAuthService = mock[OIDCAuthService]
      val userInfo = new OIDCUser("123", "name", "email", Seq("group1", "group2"))
      val bodyParser = mock[BodyParsers.Default]
      val authMock = new TestAuthenticatedUserAction(bodyParser, authprovider = oidcAuthService, userInfo = userInfo)(ec)
      val ws = mock[WSClient]
      val nexusService = mock[NexusService]
      val releaseService = mock[ReleaseService]
      val arangoQueryService = mock[ArangoQueryService]
      when(arangoQueryService.listInstances(datatype, Some(0), Some(20), "")).thenReturn(Future(Right(Json.obj("data" -> instances, "dataType"-> "http://hbp.eu/minds#Dataset", "label"->"Dataset","total" -> 2))) )
      val configService = new ConfigurationService(fakeApplication().configuration)
      val controller = new NexusEditorController(mockCC, authMock, instanceService, oidcAuthService, configService, nexusService, releaseService, arangoQueryService, ws)(ec)
      val response = controller.listInstances(datatype.org, datatype.domain, datatype.schema, datatype.version, Some(0), Some(20), "").apply(FakeRequest())
      val res = contentAsJson(response).as[JsObject]
      val arr = (res \ "data").as[List[JsObject]].map(js => js - "status" - "childrenStatus")
      val formattedRes = res ++ Json.obj("data" -> arr)
      formattedRes.toString mustBe """{"data":[{"id":"data/core/datatype/v0.0.4/123","description":"","label":"dataname1"},{"id":"data/core/datatype/v0.0.4/321","description":"","label":"dataname2"}],"dataType":"http://hbp.eu/minds#Dataset","label":"Dataset","total":2}"""

    }

  }

  "InstanceController#getSpecificReconciledInstance" should {
    "return a form with the instance" in {
      val originalDatatype = NexusPath("minds/core/activity/v0.0.4")
      val nexusPath = NexusPath("mindsreconciled/core/activity/v0.0.4".split("/"))
      val id = "123"
      val revision = 2
      import scala.concurrent.ExecutionContext.Implicits._
      val instance =
        Json.parse(
          s"""
             |{
             |    "@context": "https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/resource/v0.3.0",
             |    "@id": "https://nexus-dev.humanbrainproject.org/v0/data/${nexusPath.toString()}/$id",
             |    "@type": "http://hbp.eu/minds#Activity",
             |    "http://hbp.eu/internal#hashcode": "bd374187e78489b9b201bb885490c073",
             |    "http://hbp.eu/minds#created_at": "2018-03-26T15:21:58.362242+00:00",
             |    "http://hbp.eu/minds#ethicsApproval": {
             |        "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/ethics/approval/v0.0.4/94383d63-7587-4bc0-a834-629a9be757e9"
             |    },
             |    "http://hbp.eu/minds#ethicsAuthority": {
             |        "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/ethics/authority/v0.0.4/9bfc1378-44ca-4630-97b0-927266a0de73"
             |    },
             |    "http://hbp.eu/minds#methods": {
             |        "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/experiment/method/v0.0.4/5481f012-fa64-4b0a-8614-648f09002519"
             |    },
             |    "http://hbp.eu/minds#preparation": {
             |        "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/core/preparation/v0.0.4/33f9c5e0-0336-41c9-838a-e0a2dd74bd76"
             |    },
             |    "http://hbp.eu/minds#protocols": {
             |        "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/experiment/protocol/v0.0.4/68f34f9a-37e3-48fd-a098-86c68e1fea9d"
             |    },
             |    "http://schema.org/description": "The setting",
             |    "http://schema.org/identifier": "8016028795724369b5c4952e026dda56",
             |    "http://schema.org/name": "365.A.e.#2",
             |    "http://www.w3.org/ns/prov#qualifiedAssociation": {
             |        "@type": "http://www.w3.org/ns/prov#Association",
             |        "http://www.w3.org/ns/prov#agent": {
             |            "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/core/softwareagent/v0.0.4/91ae1e26-50cd-4928-995c-e73e313e90ea",
             |            "@type": "http://www.w3.org/ns/prov#Agent"
             |        },
             |        "http://www.w3.org/ns/prov#hadRole": {
             |            "@id": "https://nexus-dev.humanbrainproject.org/v0/data/minds/prov/role/v0.0.1/8c8b269c-eed5-4315-8edf-2441c32dbb73",
             |            "@type": "http://www.w3.org/ns/prov#Role"
             |        }
             |    },
             |    "http://hbp.eu/reconciled#original_parent":{
             |       "@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/activity/v0.0.4/321"
             |    },
             |    "nxv:rev": 2,
             |    "nxv:deprecated": false,
             |    "links": {
             |        "@context": "https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0",
             |        "incoming": "https://nexus-dev.humanbrainproject.org/v0/data/minds/core/activity/v0.0.4/005ecfd6-c63f-4c9f-bc31-fa4d64c5bf1d/incoming",
             |        "outgoing": "https://nexus-dev.humanbrainproject.org/v0/data/minds/core/activity/v0.0.4/005ecfd6-c63f-4c9f-bc31-fa4d64c5bf1d/outgoing",
             |        "schema": "https://nexus-dev.humanbrainproject.org/v0/schemas/minds/core/activity/v0.0.4",
             |        "self": "https://nexus-dev.humanbrainproject.org/v0/data/minds/core/activity/v0.0.4/005ecfd6-c63f-4c9f-bc31-fa4d64c5bf1d"
             |    }
             |}
        """.stripMargin
        )
      val fakeEndpoint = s"$nexusEndpoint/v0/data/${nexusPath.toString()}/$id?rev=$revision"
      implicit val ws = MockWS {
        case (GET, fakeEndpoint) => Action {
          Ok(instance)
        }
      }
      val mockCC = stubControllerComponents()
      val ec = global
      val oidcAuthService = mock[OIDCAuthService]
      val userInfo = new OIDCUser("123", "name", "email", Seq("group1", "group2"))
      val bodyParser = mock[BodyParsers.Default]
      val authMock = new TestAuthenticatedUserAction(bodyParser, authprovider = oidcAuthService, userInfo = userInfo)(ec)
      val nexusService = mock[NexusService]
      val releaseService = mock[ReleaseService]
      val arangoQueryService = mock[ArangoQueryService]
      val configService =  new ConfigurationService(fakeApplication().configuration)
      val instanceService = new EditorService(ws, nexusService, configService) {
        override def retrieveInstance(path: NexusPath, id: String, token: String, parameters: List[(String, String)]): Future[Either[WSResponse, NexusInstance]] = Future.successful(Right(NexusInstance(Some(id), nexusPath, instance.as[JsObject])))
      }
      val controller = new NexusEditorController(mockCC, authMock, instanceService, oidcAuthService, configService, nexusService, releaseService, arangoQueryService, ws)(ec)
      val res = contentAsString(controller.getSpecificReconciledInstance(nexusPath.org, nexusPath.domain, nexusPath.schema, nexusPath.version, id, revision).apply(FakeRequest()))
      res mustBe s"""{"fields":{"id":{"value":{"path":"minds/core/activity/v0.0.4","nexus_id":"https://nexus-dev.humanbrainproject.org/v0/data/mindsreconciled/core/activity/v0.0.4/123"}},"http:%nexus-slash%%nexus-slash%schema.org%nexus-slash%name":{"type":"InputText","label":"Name","value":"365.A.e.#2"},"http:%nexus-slash%%nexus-slash%schema.org%nexus-slash%description":{"type":"TextArea","label":"Description","value":"The setting"},"http:%nexus-slash%%nexus-slash%hbp.eu%nexus-slash%minds#ethicsApproval":{"type":"DropdownSelect","label":"Approval","instancesPath":"minds/ethics/approval/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/ethics/approval/v0.0.4/94383d63-7587-4bc0-a834-629a9be757e9"}]},"http:%nexus-slash%%nexus-slash%hbp.eu%nexus-slash%minds#ethicsAuthority":{"type":"DropdownSelect","label":"Authority","instancesPath":"minds/ethics/authority/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/ethics/authority/v0.0.4/9bfc1378-44ca-4630-97b0-927266a0de73"}]},"http:%nexus-slash%%nexus-slash%hbp.eu%nexus-slash%minds#methods":{"type":"DropdownSelect","label":"Methods","instancesPath":"minds/experiment/method/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/experiment/method/v0.0.4/5481f012-fa64-4b0a-8614-648f09002519"}]},"http:%nexus-slash%%nexus-slash%hbp.eu%nexus-slash%minds#preparation":{"type":"DropdownSelect","label":"Preparation","instancesPath":"minds/core/preparation/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/core/preparation/v0.0.4/33f9c5e0-0336-41c9-838a-e0a2dd74bd76"}]},"http:%nexus-slash%%nexus-slash%hbp.eu%nexus-slash%minds#protocols":{"type":"DropdownSelect","label":"Protocols","instancesPath":"minds/experiment/protocol/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/experiment/protocol/v0.0.4/68f34f9a-37e3-48fd-a098-86c68e1fea9d"}]}},"label":"Activity","editable":true,"ui_info":{"labelField":"http://schema.org/name","promotedFields":["http://schema.org/name","http://schema.org/description"]},"alternatives":{},"back_link":"minds/core/activity"}"""
    }
  }

  "Generate alternatives" should {
    "return a JsValue with alternatives per field" in {
      val path = NexusPath("manual", "poc", "placomponent","v0.0.4")
      val instance1 = EditorInstance(NexusInstance(Some("1"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/1","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889912,"http://hbp.eu/manual#updater_id":"123","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
      val instance2 = EditorInstance(NexusInstance(Some("2"),path, Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/2","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"456","http://schema.org/name":"B","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
      val instance3 = EditorInstance(NexusInstance(Some("3"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/3","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"789","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
      val reconciled = ReconciledInstance(NexusInstance(Some("4"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/4","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"789","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
      val seq = List(instance1, instance2, instance3).map(_.cleanManualData())
      val expectedValue = Json.obj(
        "http://schema.org/name" -> Json.arr(
          Json.obj(
            "value" -> "A",
            "updater_id" -> Json.arr("123", "789")
          ),
          Json.obj(
            "value" -> "B",
            "updater_id" -> Json.arr("456")
          )
        )
      )
      val res = reconciled.cleanManualData().generateAlternatives(seq)
      res mustBe expectedValue
    }
  }

}
