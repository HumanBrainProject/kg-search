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
import models.templates.entities.{ESFields, ESKeyword, ESPropertyObject}
import play.api.libs.json.{JsString, Json}
import utils._

trait UnimindsPersonMetaESTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "type" -> Set("properties", Json.obj("value" -> Json.obj("type" -> "keyword"))),
    "identifier" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:identifier")
    ),
    "title" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:title")
    ),
    "description" -> Set("properties", ESPropertyObject.defaultValue.toJson),
    "phone"       -> Set("properties", ESPropertyObject.defaultValue.toJson),
    "custodianOf" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:custodianOf",
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
    "custodianOfModel" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:custodianOfModel",
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
    "publications" ->
    ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:publications")
    ),
    "address" -> Set("properties", ESPropertyObject.defaultValue.toJson),
    "contributions" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:contributions",
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
    "modelContributions" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:modelContributions",
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
    "email" -> Set("properties", ESPropertyObject.defaultValue.toJson),
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
