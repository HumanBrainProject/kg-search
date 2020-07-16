/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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
package models.templates.elasticSearch

import models.templates.Template
import models.templates.entities.{ESFields, ESKeyword}
import play.api.libs.json.JsString
import utils._

trait ModelInstanceMetaESTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "type" -> Set("type", JsString("keyword")),
    "identifier" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:identifier")
    ),
    "title" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:title")
    ),
    "description" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:description")
    ),
    "version" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:version")
    ),
    "contributors" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:contributors",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "owners" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:custodian",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "publications" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:publications")
    ),
    "mainContact" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:mainContact",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "embargo" ->
    ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:embargo")
    ),
    "allfiles" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:fileBundle")
    ),
    "brainStructures" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:brainStructure")
    ),
    "cellularTarget" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:cellularTarget")
    ),
    "studyTarget" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:studyTarget")
    ),
    "modelScope" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:modelScope")
    ),
    "abstractionLevel" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:abstractionLevel")
    ),
    "modelFormat" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:modelFormat")
    ),
    "usedDataset" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:usedDataset",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "producedDataset" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:producedDataset",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "first_release" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:firstReleaseAt",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value", None))
          )
        )
      )
    ),
    "last_release" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:lastReleaseAt",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value", None))
          )
        )
      )
    )
  )
}
