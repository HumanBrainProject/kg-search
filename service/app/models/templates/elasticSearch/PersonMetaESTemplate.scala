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

trait PersonMetaESTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "identifier" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:identifier",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "title" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:title",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "description" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:description",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "phone" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:phone",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "custodianOf" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:custodianOf",
        Nested(
          "properties",
          ObjectValue(
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
          ObjectValue(
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
      ObjectReader(
        "search:publications",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "address" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:address",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "contributions" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:contributions",
        Nested(
          "properties",
          ObjectValue(
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
          ObjectValue(
            List(
              ESProperty("reference", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("uuid", Some(ESFields(ESKeyword(ignore_above = Some(256))))),
              ESProperty("value")
            )
          )
        )
      )
    ),
    "email" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:email",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
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
          ObjectValue(
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
          ObjectValue(
            List(ESProperty("value", None))
          )
        )
      )
    )
  )
}
