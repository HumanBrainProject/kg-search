package services

import helpers.ConfigMock
import mockws.MockWSHelpers
import models.{FormRegistry, NexusPath}
import org.scalatest.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient
import play.api.test.Injecting

class FormServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockWSHelpers with MockitoSugar with Injecting {
  override def fakeApplication(): Application = ConfigMock.fakeApplicationConfig.build()

  val registry = Json.parse("""    {
                                 "minds": {
                              |      "core": {
                              |        "activity": {
                              |          "v0.0.4": {
                              |            "label": "Activity",
                              |            "fields": {
                              |              "http://schema.org/name": {
                              |                "type": "InputText",
                              |                "label": "Name"
                              |              },
                              |              "http://schema.org/description": {
                              |                "type": "TextArea",
                              |                "label": "Description"
                              |              },
                              |              "http://hbp.eu/minds#ethicsApproval": {
                              |                "type": "DropdownSelect",
                              |                "label": "Approval",
                              |                "instancesPath": "minds/ethics/approval/v0.0.4",
                              |                "mappingValue": "id",
                              |                "mappingLabel": "label",
                              |                "isLink": true,
                              |                "allowCustomValues": true
                              |              },
                              |              "http://hbp.eu/minds#ethicsAuthority": {
                              |                "type": "DropdownSelect",
                              |                "label": "Authority",
                              |                "instancesPath": "minds/ethics/authority/v0.0.4",
                              |                "mappingValue": "id",
                              |                "mappingLabel": "label",
                              |                "isLink": true,
                              |                "allowCustomValues": true
                              |              },
                              |              "http://hbp.eu/minds#methods": {
                              |                "type": "DropdownSelect",
                              |                "label": "Methods",
                              |                "instancesPath": "minds/experiment/method/v0.0.4",
                              |                "mappingValue": "id",
                              |                "mappingLabel": "label",
                              |                "isLink": true,
                              |                "allowCustomValues": true
                              |              },
                              |              "http://hbp.eu/minds#preparation": {
                              |                "type": "DropdownSelect",
                              |                "label": "Preparation",
                              |                "instancesPath": "minds/core/preparation/v0.0.4",
                              |                "mappingValue": "id",
                              |                "mappingLabel": "label",
                              |                "isLink": true,
                              |                "allowCustomValues": true
                              |              },
                              |              "http://hbp.eu/minds#protocols": {
                              |                "type": "DropdownSelect",
                              |                "label": "Protocols",
                              |                "instancesPath": "minds/experiment/protocol/v0.0.4",
                              |                "mappingValue": "id",
                              |                "mappingLabel": "label",
                              |                "isLink": true,
                              |                "allowCustomValues": true
                              |              }
                              |            },
                              |            "ui_info": {
                              |              "labelField": "http://schema.org/name",
                              |              "promotedFields": [
                              |                "http://schema.org/name",
                              |                "http://schema.org/description"
                              |              ]
                              |            }
                              |          }
                              |        }
                              |      }
                              |    }
                              |    }""".stripMargin)
  "FormService#getFormStructure" should {
        "return a form with the instance content" in {
          val originalDatatype = NexusPath("minds/core/activity/v0.0.4")
          val id = "123"
          val revision = 2
          import scala.concurrent.ExecutionContext.Implicits._
          val formRegistry = FormRegistry(registry.as[JsObject])

          val config = new ConfigurationService(fakeApplication().configuration)
          val mockWs = mock[WSClient]
          val formService = new FormService(config, mockWs ){
            override def loadFormConfiguration(): FormRegistry = formRegistry
          }
          val data = Json.parse(
            s"""{
              |    "@context": "https://nexus-dev.humanbrainproject.org/v0/contexts/nexus/core/resource/v0.3.0",
              |    "@id": "https://nexus-dev.humanbrainproject.org/v0/data/${originalDatatype.toString()}/$id",
              |    "https://schema.hbp.eu/relativeUrl": "${originalDatatype.toString()}/$id",
              |    "@type": "http://hbp.eu/minds#Activity",
              |    "http://hbp.eu/internal#hashcode": "bd374187e78489b9b201bb885490c073",
              |    "http://hbp.eu/minds#created_at": "2018-03-26T15:21:58.362242+00:00",
              |    "http://hbp.eu/minds#ethicsApproval": {
              |        "https://schema.hbp.eu/relativeUrl": "minds/ethics/approval/v0.0.4/94383d63-7587-4bc0-a834-629a9be757e9"
              |    },
              |    "http://hbp.eu/minds#ethicsAuthority": {
              |        "https://schema.hbp.eu/relativeUrl": "minds/ethics/authority/v0.0.4/9bfc1378-44ca-4630-97b0-927266a0de73"
              |    },
              |    "http://hbp.eu/minds#methods": {
              |        "https://schema.hbp.eu/relativeUrl": "minds/experiment/method/v0.0.4/5481f012-fa64-4b0a-8614-648f09002519"
              |    },
              |    "http://schema.org/name": "365.A.e.#2"
              |    }
              |    """.stripMargin)

          val res = FormService.getFormStructure(originalDatatype, data.as[JsObject], formRegistry)
          val expected = Json.parse(
            """
              | {"fields":{"id":{"value":{"path":"minds/core/activity/v0.0.4"},"nexus_id":"minds/core/activity/v0.0.4/123"},"http://schema.org/name":{"type":"InputText","label":"Name","value":"365.A.e.#2"},"http://schema.org/description":{"type":"TextArea","label":"Description"},"http://hbp.eu/minds#ethicsApproval":{"type":"DropdownSelect","label":"Approval","instancesPath":"minds/ethics/approval/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/ethics/approval/v0.0.4/94383d63-7587-4bc0-a834-629a9be757e9"}]},"http://hbp.eu/minds#ethicsAuthority":{"type":"DropdownSelect","label":"Authority","instancesPath":"minds/ethics/authority/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/ethics/authority/v0.0.4/9bfc1378-44ca-4630-97b0-927266a0de73"}]},"http://hbp.eu/minds#methods":{"type":"DropdownSelect","label":"Methods","instancesPath":"minds/experiment/method/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true,"value":[{"id":"minds/experiment/method/v0.0.4/5481f012-fa64-4b0a-8614-648f09002519"}]},"http://hbp.eu/minds#preparation":{"type":"DropdownSelect","label":"Preparation","instancesPath":"minds/core/preparation/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true},"http://hbp.eu/minds#protocols":{"type":"DropdownSelect","label":"Protocols","instancesPath":"minds/experiment/protocol/v0.0.4","mappingValue":"id","mappingLabel":"label","isLink":true,"allowCustomValues":true}},"label":"Activity","editable":true,"ui_info":{"labelField":"http://schema.org/name","promotedFields":["http://schema.org/name","http://schema.org/description"]},"alternatives":{}}
            """.stripMargin)
          res mustBe expected
        }
      }

}
