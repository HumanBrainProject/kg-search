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
    "zip" ->
      ObjectReader(
        "fields",
        ObjectReader(
          "search:containerUrlAsZIP",
          Nested(
            "properties",
            WriteObject(
              List(ESProperty("value"))
            )
          )
        )
      ),
    "citation" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:citation",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "dataDescriptor" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:dataDescriptorURL",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "doi" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:doi",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "license_info" -> ObjectReader(
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
    ),
    "component" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:component",
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
        "search:owners",
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
    "speciesFilter" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:speciesFilter",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "embargoForFilter" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:embargoForFilter",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "embargo" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:embargo",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "files" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:files",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "external_datalink" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:external_datalink",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "publications" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:publications",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "atlas" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:parcellationAtlas",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "region" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:parcellationRegion",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "preparation" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:preparation",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "methods" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:methods",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "protocol" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:protocols",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "viewer" -> ObjectReader(
      "fields",
      OrElse(
        ObjectReader(
          "search:brainviewer",
          Nested(
            "properties",
            WriteObject(
              List(ESProperty("value"))
            )
          )
        ),
        ObjectReader(
          "search:neuroglancer",
          Nested(
            "properties",
            WriteObject(
              List(ESProperty("value"))
            )
          )
        )
      )
    ),
    "subjects" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:subjects",
        Nested(
          "properties",
          WriteObject(
            List(
              ESProperty("value"),
              Nested(
                "children",
                WriteObject(
                  List(
                    Set("type", JsString("nested")),
                    ObjectReader(
                      "fields",
                      Nested(
                        "properties",
                        WriteObject(
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
                                WriteObject(
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
