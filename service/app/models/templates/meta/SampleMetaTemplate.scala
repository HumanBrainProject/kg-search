package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectMap
import play.api.libs.json.{JsNull, JsObject}
import utils._

trait SampleMetaTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "fields" -> ObjectReader(
      "fields",
      WriteObject(
        List(
          Nested(
            "identifier",
            ObjectReader(
              "search:identifier",
              PrimitiveToObjectWithCustomField[Boolean](
                "searchUi:visible",
                "https://schema.hbp.eu/searchUi/visible"
              )
            )
          ),
          Nested(
            "title",
            ObjectReader(
              "search:title",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
                )
              )
            )
          ),
          Nested(
            "editorId",
            ObjectReader(
              "search:editorId",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "weightPreFixation",
            ObjectReader(
              "search:weightPreFixation",
              PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
            )
          ),
          Nested(
            "parcellationAtlas",
            ObjectReader(
              "search:parcellationAtlas",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "region",
            ObjectReader(
              "search:parcellationRegion",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:link_icon",
                    "https://schema.hbp.eu/searchUi/link_icon"
                  )
                )
              )
            )
          ),
          Nested(
            "viewer",
            ObjectReader(
              "search:brainViewer",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "methods",
            ObjectReader(
              "search:methods",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:tag_icon",
                    "https://schema.hbp.eu/searchUi/tag_icon"
                  ),
                  PrimitiveToObjectWithCustomField[Int](
                    "searchUi:overviewMaxDisplay",
                    "https://schema.hbp.eu/searchUi/overviewMaxDisplay"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  )
                )
              )
            )
          ),
          Nested(
            "allfiles",
            ObjectReader(
              "search:container_url",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isButton",
                    "https://schema.hbp.eu/searchUi/isButton"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:termsOfUse",
                    "https://schema.hbp.eu/searchUi/termsOfUse"
                  )
                )
              )
            )
          ),
          Nested(
            "files",
            ObjectReader(
              "search:files",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:termsOfUse",
                    "https://schema.hbp.eu/searchUi/termsOfUse"
                  )
                )
              )
            )
          ),
          Nested(
            "datasetExists",
            ObjectReader(
              "search:datasetExists",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:facet",
                    "https://schema.hbp.eu/searchUi/facet"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "datasets",
            ObjectReader(
              "search:datasets",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  ObjectReader(
                    "fields",
                    Nested(
                      "children",
                      WriteObject(
                        List(
                          Nested(
                            "component",
                            ObjectReader(
                              "search:componentName",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "searchUi:type",
                                    "https://schema.hbp.eu/searchUi/type"
                                  ),
                                )
                              )
                            )
                          ),
                          Set("name", JsObject.empty)
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "subject",
            ObjectReader(
              "search:subjects",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  ),
                  ObjectReader(
                    "fields",
                    Nested(
                      "children",
                      WriteObject(
                        List(
                          Nested(
                            "subject_name",
                            ObjectReader(
                              "search:name",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "species",
                            ObjectReader(
                              "search:species",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "searchUi:facet",
                                    "https://schema.hbp.eu/searchUi/facet"
                                  ),
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                  PrimitiveToObjectWithCustomField[String](
                                    "searchUi:type",
                                    "https://schema.hbp.eu/searchUi/type"
                                  ),
                                  PrimitiveToObjectWithCustomField[String](
                                    "searchUi:facet_order",
                                    "https://schema.hbp.eu/searchUi/facet_order"
                                  ),
                                  PrimitiveToObjectWithCustomField[Boolean](
                                    "searchUi:overview",
                                    "https://schema.hbp.eu/searchUi/overview"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "sex",
                            ObjectReader(
                              "search:sex",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "searchUi:facet",
                                    "https://schema.hbp.eu/searchUi/facet"
                                  ),
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "age",
                            ObjectReader(
                              "search:age",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "agecategory",
                            ObjectReader(
                              "search:agecategory",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "weight",
                            ObjectReader(
                              "search:weight",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "strain",
                            ObjectReader(
                              "search:strain",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                          Nested(
                            "genotype",
                            ObjectReader(
                              "search:genotype",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                )
                              )
                            )
                          ),
                        ),
                      )
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "first_release",
            ObjectReader(
              "search:firstReleaseAt",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:ignoreForSearch",
                    "https://schema.hbp.eu/searchUi/ignoreForSearch",
                    identity
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
                )
              )
            )
          ),
          Nested(
            "last_release",
            ObjectReader(
              "search:lastReleaseAt",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:ignoreForSearch",
                    "https://schema.hbp.eu/searchUi/ignoreForSearch",
                    identity
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
                )
              )
            )
          )
        )
      )
    ),
    "https://schema.hbp.eu/searchUi/icon"  -> Get[String]("searchUi:icon"),
    "http://schema.org/identifier"         -> Get[String]("http://schema.org/identifier"),
    "https://schema.hbp.eu/searchUi/order" -> Get[Int]("searchUi:order"),
    "http://schema.org/name"               -> Get[String]("schema:name")
  )
}
