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
package controllers

import constants.SchemaFieldsConstants
import helpers.ConfigMock
import mockws.MockWSHelpers
import models.NexusPath
import models.instance.{EditorInstance, NexusInstance}
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json._
import play.api.test.Injecting

class EditorControllerSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with MockWSHelpers
    with MockitoSugar
    with Injecting {

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
//  "InstanceController#list" should {
//    "return a 200 with a correctly formatted result" in {
//      val datatype = NexusPath("data/core/datatype/v0.0.4".split("/"))
//      val nexusBase = "http://nexus.org/v0/data"
//      import scala.concurrent.ExecutionContext.Implicits._
//      val instances = Json.arr(
//        Json.obj("id" -> s"${datatype.toString()}/123", "description"->"","label" -> "dataname1"),
//        Json.obj("id" -> s"${datatype.toString()}/321", "description"->"","label" -> "dataname2")
//      )
//      val fakeEndpoint = s"$kgQueryEndpoint/arango/instances/${datatype.toString()}"
//
//      val mockCC = stubControllerComponents()
//      val ec = global
//      val instanceService = mock[EditorService]
//
//      val oidcAuthService = mock[OIDCAuthService]
//      val bodyParser = mock[BodyParsers.Default]
//      val authMock = new TestAuthenticatedUserAction(bodyParser, authprovider = oidcAuthService, userInfo = userInfo)(ec)
//      val ws = mock[WSClient]
//      val nexusService = mock[NexusService]
//      val releaseService = mock[ReleaseService]
//      when(arangoQueryService.listInstances(datatype, Some(0), Some(20), "")).thenReturn(Future(Right(Json.obj("data" -> instances, "dataType"-> "http://hbp.eu/minds#Dataset", "label"->"Dataset","total" -> 2))) )
//      val configService = new ConfigurationService(fakeApplication().configuration)
//      val formService = mock[FormService]
//      val iamAuth = mock[IAMAuthService]
//      val controller = new NexusEditorController(mockCC, authMock, instanceService, oidcAuthService, configService, nexusService, releaseService, arangoQueryService, iamAuth, formService, ws)(ec)
//      val response = controller.listInstances(datatype.org, datatype.domain, datatype.schema, datatype.version, Some(0), Some(20), "").apply(FakeRequest())
//      val res = contentAsJson(response).as[JsObject]
//      val arr = (res \ "data").as[List[JsObject]].map(js => js - "status" - "childrenStatus")
//      val formattedRes = res ++ Json.obj("data" -> arr)
//      formattedRes.toString mustBe """{"data":[{"id":"data/core/datatype/v0.0.4/123","description":"","label":"dataname1"},{"id":"data/core/datatype/v0.0.4/321","description":"","label":"dataname2"}],"dataType":"http://hbp.eu/minds#Dataset","label":"Dataset","total":2}"""
//
//    }
//
//  }

//  "Generate alternatives" should {
//    "return a JsValue with alternatives per field" in {
//      val path = NexusPath("manual", "poc", "placomponent","v0.0.4")
//      val instance1 = EditorInstance(NexusInstance(Some("1"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/1","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889912,"http://hbp.eu/manual#updater_id":"123","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
//      val instance2 = EditorInstance(NexusInstance(Some("2"),path, Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/2","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"456","http://schema.org/name":"B","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
//      val instance3 = EditorInstance(NexusInstance(Some("3"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/3","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"789","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
//      val reconciled = ReconciledInstance(NexusInstance(Some("4"),path,Json.parse("""{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/4","@type":"http://hbp.eu/manual#Placomponent","http://hbp.eu/manual#origin":"d34061a2-630b-40bd-99b0-1052cd66f740","http://hbp.eu/manual#parent":{"@id":"https://nexus-dev.humanbrainproject.org/v0/data/minds/core/placomponent/v0.0.4/d34061a2-630b-40bd-99b0-1052cd66f740"},"http://hbp.eu/manual#update_timestamp":1528987889913,"http://hbp.eu/manual#updater_id":"789","http://schema.org/name":"A","nxv:rev":6,"nxv:deprecated":false,"links":{"@context":"https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/links/v0.2.0","incoming":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/incoming","outgoing":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e/outgoing","schema":"https://nexus-dev.humanbrainproject.org/v0/schemas/manual/poc/placomponent/v0.0.4","self":"https://nexus-dev.humanbrainproject.org/v0/data/manual/poc/placomponent/v0.0.4/31cffb99-c932-4f03-b17b-799a2525f65e"}}""").as[JsObject]))
//      val seq = List(instance1, instance2, instance3).map(_.cleanManualData())
//      val expectedValue = Json.obj(
//        SchemaFieldsConstants.NAME -> Json.arr(
//          Json.obj(
//            "value" -> "A",
//            "updater_id" -> Json.arr("123", "789")
//          ),
//          Json.obj(
//            "value" -> "B",
//            "updater_id" -> Json.arr("456")
//          )
//        )
//      )
//      val res = reconciled.cleanManualData().generateAlternatives(seq)
//      res mustBe expectedValue
//    }
//  }

}
