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

trait DatasetMetaESTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "identifier" -> ObjectReader(
      "search:identifier",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "title" -> ObjectReader(
      "search:title",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "contributors" -> ObjectReader(
      "search:contributors",
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
    ),
    "zip" ->
    ObjectReader(
      "search:containerUrlAsZIP",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "citation" -> ObjectReader(
      "search:citation",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "dataDescriptor" -> ObjectReader(
      "search:dataDescriptorURL",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "doi" -> ObjectReader(
      "search:doi",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "license_info" -> ObjectReader(
      "search:license",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "component" -> ObjectReader(
      "search:component",
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
    ),
    "owners" -> ObjectReader(
      "search:owners",
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
    ),
    "description" -> ObjectReader(
      "search:description",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "speciesFilter" -> ObjectReader(
      "search:speciesFilter",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "embargoForFilter" -> ObjectReader(
      "search:embargoForFilter",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "embargo" -> ObjectReader(
      "search:embargo",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "files" -> ObjectReader(
      "search:files",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "external_datalink" -> ObjectReader(
      "search:external_datalink",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "publications" -> ObjectReader(
      "search:publications",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "atlas" -> ObjectReader(
      "search:parcellationAtlas",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "region" -> ObjectReader(
      "search:parcellationRegion",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "preparation" -> ObjectReader(
      "search:preparation",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "methods" -> ObjectReader(
      "search:methods",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "protocol" -> ObjectReader(
      "search:protocols",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value"))
        )
      )
    ),
    "viewer" -> OrElse(
      ObjectReader(
        "search:brainviewer",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      ),
      ObjectReader(
        "search:neuroglancer",
        Nested(
          "properties",
          ObjectValue(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "subjects" -> ObjectReader(
      "search:subjects",
      Nested(
        "properties",
        ObjectValue(
          List(
            ESProperty("value"),
            Nested(
              "children",
              ObjectValue(
                List(
                  Set("type", JsString("nested")),
                  ObjectReader(
                    "fields",
                    Nested(
                      "properties",
                      ObjectValue(
                        List(
                          Nested(
                            "subject_name",
                            Nested(
                              "properties",
                              ESProperty("value")
                            )
                          ),
                          Nested(
                            "species",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "sex",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "age",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "agecategory",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "weight",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "strain",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "genotype",
                            Nested("properties", ESProperty("value"))
                          ),
                          Nested(
                            "samples",
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
                          ),
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )
    ),
    "first_release" -> ObjectReader(
      "search:firstReleaseAt",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value", None))
        )
      )
    ),
    "last_release" -> ObjectReader(
      "search:lastReleaseAt",
      Nested(
        "properties",
        ObjectValue(
          List(ESProperty("value", None))
        )
      )
    ),
  )
}
