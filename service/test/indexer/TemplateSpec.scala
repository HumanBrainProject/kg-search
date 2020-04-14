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
import models.templates.elasticSearch.{
  DatasetMetaESTemplate,
  PersonMetaESTemplate,
  ProjectMetaESTemplate,
  SampleMetaESTemplate
}
import models.templates.instance.{
  DatasetTemplate,
  ModelInstanceTemplate,
  PersonTemplate,
  ProjectTemplate,
  SampleTemplate,
  SoftwareProjectTemplate,
  UnimindsPersonTemplate
}
import models.templates.meta.{DatasetMetaTemplate, ProjectMetaTemplate, SampleMetaTemplate}
import models.{DatabaseScope, INFERRED}
import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Injecting
import services.indexer.IndexerImpl

class TemplateSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  def loadResource(filename: String): JsValue = {
    val jsonFile = getClass.getResource(filename).getFile
    val stream = new FileInputStream(jsonFile)
    Json.parse(stream)
  }

  def assertIsSameJsObject(fieldName: String, result: JsValue, expected: JsValue): Assertion = {
    assert(
      result.as[JsObject].value.get(fieldName) == expected.as[JsObject].value.get(fieldName)
    )
  }
  "The template engine" must {
    "transform correctly a query for datasets" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/dataset/dataset.json")
      val template = new DatasetTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/dataset/expectedDataset.json")
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
      val payload = loadResource("/person/person.json")
      val template = new PersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/person/expectedPerson.json")
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
    "transform the uniminds person payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/unimindsPerson.json")
      val template = new UnimindsPersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedUnimindsPerson.json")
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
      val payload = loadResource("/project/project.json")
      val template = new ProjectTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/project/expectedProject.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("dataset", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "transform the software project payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/softwareProject.json")
      val template = new SoftwareProjectTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedSoftwareProject.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("appCategory", result, expected)
      assertIsSameJsObject("sourceCode", result, expected)
      assertIsSameJsObject("documentation", result, expected)
      assertIsSameJsObject("license", result, expected)
      assertIsSameJsObject("operatingSystem", result, expected)
      assertIsSameJsObject("homepage", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("version", result, expected)
    }
    "transform the sample payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/sample/sample.json")
      val template = new SampleTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/sample/expectedSample.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("weightPreFixation", result, expected)
      assertIsSameJsObject("parcellationAtlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("allfiles", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("subject", result, expected)
      assertIsSameJsObject("datasetExists", result, expected)
      assertIsSameJsObject("datasets", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "transform the model instance payload accordingly" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/modelInstance.json")
      val template = new ModelInstanceTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedModelInstance.json")
      assertIsSameJsObject("producedDataset", result, expected)
      assertIsSameJsObject("modelFormat", result, expected)
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("owners", result, expected)
      assertIsSameJsObject("abstractionLevel", result, expected)
      assertIsSameJsObject("mainContact", result, expected)
      assertIsSameJsObject("brainStructures", result, expected)
      assertIsSameJsObject("usedDataset", result, expected)
      assertIsSameJsObject("version", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("studyTarget", result, expected)
      assertIsSameJsObject("allFiles", result, expected)
      assertIsSameJsObject("modelScope", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("contributors", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("last_release", result, expected)
      assertIsSameJsObject("cellularTarget", result, expected)
    }
    "properly handle empty values in dataset" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/dataset/emptyDataset.json")
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
      val payload = loadResource("/person/emptyPerson.json")
      val template = new PersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/person/expectedEmptyPerson.json")
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
    "properly handle empty values in uniminds person" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/emptyUnimindsPerson.json")
      val template = new UnimindsPersonTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/expectedEmptyUnimindsPerson.json")
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
      val payload = loadResource("/project/emptyProject.json")
      val template = new ProjectTemplate {
        override def fileProxy: String = ""
        override def dataBaseScope: DatabaseScope = INFERRED
      }
      val result = indexer.transform(payload, template)
      val expected = loadResource("/project/expectedEmptyProject.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("dataset", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("editorId", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
  }
  "The meta template engine" must {
    "create an object for the meta information for dataset" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/dataset/datasetMeta.json")
      val template = new DatasetMetaTemplate {}
      val result = indexer.transformMeta(payload, template).as[JsObject].value("fields")
      val expected = loadResource("/dataset/expectedDatasetMeta.json")
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
    "create an object for meta information for Project" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/project/projectMeta.json")
      val template = new ProjectMetaTemplate {}
      val result = indexer.transformMeta(payload, template).as[JsObject].value("fields")
      val expected = loadResource("/project/expectedProjectMeta.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("datasets", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "create an object for meta information for Sample" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/sample/sampleMeta.json")
      val template = new SampleMetaTemplate {}
      val result = indexer.transformMeta(payload, template).as[JsObject].value("fields")
      val expected = loadResource("/sample/expectedSampleMeta.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("weightPreFixation", result, expected)
      assertIsSameJsObject("parcellationAtlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("allfiles", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("subject", result, expected)
      assertIsSameJsObject("datasetExists", result, expected)
      assertIsSameJsObject("datasets", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }

  }

  "The ES template engine" must {
    "create an object for ES mapping information for Dataset" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/dataset/datasetMeta.json")
      val template = new DatasetMetaESTemplate {}
      val result = indexer.transformMeta(payload, template)
      val expected = loadResource("/dataset/expectedDatasetMetaES.json")
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
    "create an object for ES mapping information for Person" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/person/personMeta.json")
      val template = new PersonMetaESTemplate {}
      val result = indexer.transformMeta(payload, template)
      val expected = loadResource("/person/expectedPersonMetaES.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("phone", result, expected)
      assertIsSameJsObject("custodianOf", result, expected)
      assertIsSameJsObject("custodianOfModel", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("address", result, expected)
      assertIsSameJsObject("contributions", result, expected)
      assertIsSameJsObject("modelContributions", result, expected)
      assertIsSameJsObject("email", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "create an object for ES mapping information for Project" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/project/projectMeta.json")
      val template = new ProjectMetaESTemplate {}
      val result = indexer.transformMeta(payload, template)
      val expected = loadResource("/project/expectedProjectMetaES.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("description", result, expected)
      assertIsSameJsObject("datasets", result, expected)
      assertIsSameJsObject("publications", result, expected)
      assertIsSameJsObject("first_release", result, expected)
      assertIsSameJsObject("last_release", result, expected)
    }
    "create an object for ES mapping information for Sample" in {
      val indexer = app.injector.instanceOf[IndexerImpl]
      val payload = loadResource("/sample/sampleMeta.json")
      val template = new SampleMetaESTemplate {}
      val result = indexer.transformMeta(payload, template)
      val expected = loadResource("/sample/expectedSampleMetaES.json")
      assertIsSameJsObject("identifier", result, expected)
      assertIsSameJsObject("title", result, expected)
      assertIsSameJsObject("weightPreFixation", result, expected)
      assertIsSameJsObject("parcellationAtlas", result, expected)
      assertIsSameJsObject("region", result, expected)
      assertIsSameJsObject("viewer", result, expected)
      assertIsSameJsObject("methods", result, expected)
      assertIsSameJsObject("allfiles", result, expected)
      assertIsSameJsObject("files", result, expected)
      assertIsSameJsObject("subject", result, expected)
      assertIsSameJsObject("datasetExists", result, expected)
      assertIsSameJsObject("datasets", result, expected)
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
