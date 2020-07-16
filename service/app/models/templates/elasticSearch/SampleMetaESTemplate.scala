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

trait SampleMetaESTemplate extends Template {

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
    "weightPreFixation" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:weightPreFixation",
        Nested(
          "properties",
          WriteObject(
            List(ESProperty("value"))
          )
        )
      )
    ),
    "parcellationAtlas" -> ObjectReader(
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
    "viewer" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:brainViewer",
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
    "allfiles" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:container_url",
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
    "subject" -> ObjectReader(
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
    "datasetExists" -> ObjectReader(
      "fields",
      TemplateHelper.defaultESMapping("search:datasetExists")
    ),
    "datasets" -> ObjectReader(
      "fields",
      ObjectReader(
        "search:datasets",
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
                              "component",
                              Nested(
                                "properties",
                                ESProperty("value")
                              )
                            ),
                            Nested(
                              "name",
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
