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
package indexer

import java.io.FileInputStream

import controllers.IndexerController
import models.templates.Dataset
import models.templates.instance.{DatasetTemplate, PersonTemplate, ProjectTemplate}
import models.templates.meta.DatasetMetaTemplate
import models.{DatabaseScope, INFERRED}
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Injecting
import services.indexer.IndexerImpl

class IndexerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  def loadResource(filename: String): JsValue = {
    val jsonFile = getClass.getResource(filename).getFile
    val stream = new FileInputStream(jsonFile)
    Json.parse(stream)
  }

  def assertIsSameJsObject(fieldName: String, result: JsValue, expected: JsValue): Assertion = {
    assert(
      result.as[JsObject].value.get(fieldName).toString == expected.as[JsObject].value.get(fieldName).toString
    )
  }
  "The indexed trait" must {
    "transform the dataset payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/dataset.json")
      val template = new DatasetTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedDataset.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("contributors", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("citation", result, expected)
      assertIsSameJsObject("license_info", result, expected)
      assertIsSameJsObject("doi", result, expected)
      assertIsSameJsObject("component", result, expected)
      assertIsSameJsObject("owners", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("speciesFilter", result, expected)
      assertIsSameJsObject("embargoForFilter", result, expected)
      assertIsSameJsObject("embargo", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("dataDescriptor", result, expected)
      assertIsSameJsObject("external_datalink", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("atlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("preparation", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("protocols", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("subjects", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "transform the person payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/person.json")
      val template = new PersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedPerson.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("address", result, expected)
      assertIsSameJsObject("custodianOfModel", result, expected)
      assertIsSameJsObject("lastReleaseAt", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("modelContributions", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("firstReleaseAt", result, expected)
      assertIsSameJsObject("custodianOf", result, expected)
      assertIsSameJsObject("contributions", result, expected)
      assertIsSameJsObject("phone", result, expected)
      assertIsSameJsObject("organizations", result, expected)
      assertIsSameJsObject("email", result, expected)
      assertIsSameJsObject("publications", result, expected)
    }
    "transform the project payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/project.json")
      val template = new ProjectTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedProject.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("dataset", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "properly handle empty values in dataset" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/emptyDataset.json")
      val template = new DatasetTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedEmptyDataset.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("contributors", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("citation", result, expected)
      assertIsSameJsObject("license_info", result, expected)
      assertIsSameJsObject("doi", result, expected)
      assertIsSameJsObject("component", result, expected)
      assertIsSameJsObject("owners", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("speciesFilter", result, expected)
      assertIsSameJsObject("embargoForFilter", result, expected)
      assertIsSameJsObject("embargo", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("external_datalink", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("atlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("preparation", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("protocols", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("subjects", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "properly handle empty values in person" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/emptyPerson.json")
      val template = new PersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedEmptyPerson.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("address", result, expected)
      assertIsSameJsObject("custodianOfModel", result, expected)
      assertIsSameJsObject("lastReleaseAt", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("modelContributions", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("firstReleaseAt", result, expected)
      assertIsSameJsObject("custodianOf", result, expected)
      assertIsSameJsObject("contributions", result, expected)
      assertIsSameJsObject("phone", result, expected)
      assertIsSameJsObject("email", result, expected)
      assertIsSameJsObject("publications", result, expected)
    }
    "properly handle empty values in project" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/emptyProject.json")
      val template = new ProjectTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedEmptyProject.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("dataset", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "create an object for the meta information" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/datasetMeta.json")
      val template = new DatasetMetaTemplate {}
      val result = indexer.transformMeta(payload, template)
      val expected = loadResource("/expectedDatasetMeta.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("contributors", result, expected)
      assertIsSameJsObject("zip", result, expected)
      assertIsSameJsObject("citation", result, expected)
      assertIsSameJsObject("dataDescriptor", result, expected)
      assertIsSameJsObject("doi", result, expected)
      assertIsSameJsObject("license_info", result, expected)
      assertIsSameJsObject("component", result, expected)
      assertIsSameJsObject("owners", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("speciesFilter", result, expected)
      assertIsSameJsObject("embargoForFilter", result, expected)
      assertIsSameJsObject("embargo", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("external_datalink", result, expected)
      assertIsSameJsObject("atlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("preparation", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("protocol", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("subjects", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
  }

  "The index endpoint" must {
    "fetch datasets and apply the template" in {
      val indexerController = app.injector.instanceOf[IndexerController]
    }
  }
}
