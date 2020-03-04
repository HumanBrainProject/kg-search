/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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

import helpers.excel.ExcelUnimindsExportHelper
import models.excel.{ArrayValue, Entity, SingleValue}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import org.scalatest._
import Matchers._

class ExcelUnimindsExportHelperSpec extends PlaySpec with GuiceOneAppPerSuite {
  val defaultValueSingle = "Single value placeholder"
  val defaultValueLink = "_Link placeholder"
  "Generate entity from spec" should {
    "Create entity with correct fields" in {
      val result =
        ExcelUnimindsExportHelper.generateEntityFromQuerySpec("subjectgroup", Json.parse(singleEntity).as[JsObject])
      val content = Map(
        "http://schema.org/name"        -> SingleValue(defaultValueSingle, None, None, "Alias"),
        "http://schema.org/description" -> SingleValue(defaultValueSingle, None, None, "Description")
      )
      val expected = Entity("subjectgroup", "", content)

      result.rawType shouldBe expected.rawType
      result.rawContent should contain theSameElementsAs content
    }

    "Create entities with correct fields" in {
      val results = ExcelUnimindsExportHelper.generateEntitiesFromQuerySpec(Json.parse(spec).as[JsObject])
      val subjectGroup = Map(
        "http://schema.org/name"        -> SingleValue(defaultValueSingle, None, None, "Alias"),
        "http://schema.org/description" -> SingleValue(defaultValueSingle, None, None, "Description")
      )
      val subject = Map(
        "http://schema.org/name" -> SingleValue(defaultValueSingle, None, None, "Alias"),
        "https://schema.hbp.eu/uniminds/species" -> ArrayValue(
          Seq(SingleValue(defaultValueLink, None, None, "Species"))
        ),
        "https://schema.hbp.eu/uniminds/brainstructure" -> ArrayValue(
          Seq(SingleValue(defaultValueLink, None, None, "Brain structure"))
        )
      )
      val expected = Seq(
        Entity("subject", "", subject),
        Entity("subjectgroup", "", subjectGroup),
      )
      results should contain theSameElementsAs expected
    }
  }

  val singleEntity =
    """{

                       |        "v1.0.0": {
                       |            "label": "Subject group",
                       |            "fields": [
                       |                {
                       |                    "key": "http://schema.org/name",
                       |                    "type": "InputText",
                       |                    "label": "Alias",
                       |                    "uiDirective": {
                       |                        "labelTooltip": "The alias should be short and descriptive. It needs to be unique across all subject groups within a project. It should be descriptive of the subject group, as it will be used to retrieve information about the subject."
                       |                    }
                       |                },
                       |                {
                       |                    "key": "http://schema.org/description",
                       |                    "type": "TextArea",
                       |                    "label": "Description",
                       |                    "uiDirective": {
                       |                        "labelTooltip": "Enter a short description of the subject group."
                       |                    }
                       |                }
                       |            ]
                       |        }
                       |}""".stripMargin

  val spec =
    """{
      |    "uniminds": {
      |      "core": {
      |      "subject": {
      |        "v1.0.0": {
      |          "label": "Subject",
      |          "fields": [
      |            {
      |              "key": "http://schema.org/name",
      |              "type": "InputText",
      |              "label": "Alias"
      |            },
      |            {
      |              "key": "https://schema.hbp.eu/uniminds/species",
      |              "type": "DropdownSelect",
      |              "closeDropdownAfterInteraction": true,
      |              "label": "Species",
      |              "instancesPath": "uniminds/options/species/v1.0.0",
      |              "isLink": true,
      |              "mappingValue": "id",
      |              "mappingLabel": "name",
      |              "allowCustomValues": true
      |            },
      |            {
      |              "key": "https://schema.hbp.eu/uniminds/brainstructure",
      |              "type": "DropdownSelect",
      |              "closeDropdownAfterInteraction": true,
      |              "label": "Brain structure",
      |              "instancesPath": "uniminds/options/brainstructure/v1.0.0",
      |              "isLink": true,
      |              "mappingValue": "id",
      |              "mappingLabel": "name",
      |              "allowCustomValues": true
      |            }
      |          ],
      |          "folderID": "UNIMINDS",
      |          "folderName": "Uniminds",
      |          "ui_info": {
      |            "labelField": "http://schema.org/name",
      |            "promotedFields": [
      |              "http://schema.org/name"
      |            ]
      |          }
      |        }
      |      },
      |        "subjectgroup": {
      |          "v1.0.0": {
      |            "label": "Subject group",
      |            "fields": [
      |              {
      |                "key": "http://schema.org/name",
      |                "type": "InputText",
      |                "label": "Alias",
      |                "uiDirective": {
      |                  "labelTooltip": "The alias should be short and descriptive. It needs to be unique across all subject groups within a project. It should be descriptive of the subject group, as it will be used to retrieve information about the subject."
      |                }
      |              },
      |              {
      |                "key": "http://schema.org/description",
      |                "type": "TextArea",
      |                "label": "Description",
      |                "uiDirective": {
      |                  "labelTooltip": "Enter a short description of the subject group."
      |                }
      |              }
      |            ],
      |            "folderID": "UNIMINDS",
      |            "folderName": "Uniminds",
      |            "ui_info": {
      |              "labelField": "http://schema.org/name",
      |              "promotedFields": [
      |                "http://schema.org/name"
      |              ]
      |            }
      |          }
      |        }
      |      }
      |    }
      |}""".stripMargin
}
