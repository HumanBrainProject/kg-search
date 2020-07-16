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

trait SoftwareProjectMetaESTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "type" -> Set("type", JsString("keyword")),
    "identifier" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:identifier",
        Nested(
          "properties",
          WriteObject(
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
          WriteObject(
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
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "license" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:license",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "version" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:version",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "appCategory" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:applicationCategory",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "operatingSystem" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:operatingSystem",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "homepage" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:homepage",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "sourceCode" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:sourceCode",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "documentation" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:documentation",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    ),
    "features" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:versions",
        ObjectReader(
          "fields",
          ObjectReader(
            "search:features",
            Nested(
              "properties",
              WriteObject(
                List(ESProperty("value"))
              )
            )
          )
        )
      )
    )
  )
}
