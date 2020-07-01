package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectMap
import utils.{Get, Merge, Nested, ObjectReader, OrElse, PrimitiveToObjectWithCustomField, TemplateComponent, WriteObject}

trait DatasetMetaTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "fields" -> ObjectReader(
      "fields",
      WriteObject(
        List(
          Nested(
            "identifier",
            ObjectReader(
              "search:identifier",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:ignoreForSearch",
                    "https://schema.hbp.eu/searchUi/ignoreForSearch"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  )
                )
              )
            )
          ),
          Nested(
            "title",
            ObjectReader(
              "search:title",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:optional",
                    "https://schema.hbp.eu/searchUi/optional"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
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
            "contributors",
            ObjectReader(
              "search:contributors",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:label_hidden",
                    "https://schema.hbp.eu/searchUi/label_hidden"
                  )
                )
              )
            )
          ),
          Nested(
            "zip",
            Merge(
              ObjectReader(
                "search:containerUrlAsZIP",
                WriteObject(
                  List(
                    PrimitiveToObjectWithCustomField[Boolean](
                      "searchUi:isButton",
                      "https://schema.hbp.eu/searchUi/isButton"
                    ),
                    PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                  )
                )
              ),
              ObjectReader(
                "search:files",
                WriteObject(
                  List(
                    PrimitiveToObjectWithCustomField[Boolean](
                      "searchUi:termsOfUse",
                      "https://schema.hbp.eu/searchUi/termsOfUse"
                    ),
                  )
                )
              ), {
                case (Some(ObjectMap(zip)), Some(ObjectMap(List(termOfUse)))) =>
                  Some(ObjectMap(zip :+ termOfUse))
                case _ => None
              }
            )
          ),
          Nested(
            "citation",
            ObjectReader(
              "search:citation",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isButton",
                    "https://schema.hbp.eu/searchUi/isButton"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "dataDescriptor",
            ObjectReader(
              "search:dataDescriptorURL",
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
                  ),
                )
              )
            )
          ),
          Nested(
            "doi",
            ObjectReader(
              "search:doi",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "license_info",
            ObjectReader(
              "search:license",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:facet_order",
                    "https://schema.hbp.eu/searchUi/facet_order"
                  )
                )
              )
            )
          ),
          Nested(
            "component",
            ObjectReader(
              "search:component",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
                )
              )
            )
          ),
          Nested(
            "owners",
            ObjectReader(
              "search:owners",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
                )
              )
            )
          ),
          Nested(
            "description",
            ObjectReader(
              "search:description",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:label_hidden",
                    "https://schema.hbp.eu/searchUi/label_hidden"
                  )
                )
              )
            )
          ),
          Nested(
            "speciesFilter",
            ObjectReader(
              "search:speciesFilter",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
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
            "embargoRestrictedAccess",
            ObjectReader(
              "search:embargoRestrictedAccess",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "embargoForFilter",
            ObjectReader(
              "search:embargoForFilter",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "embargo",
            ObjectReader(
              "search:embargo",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
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
                    "searchUi:isHierarchicalFiles",
                    "https://schema.hbp.eu/searchUi/isHierarchicalFiles"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:termsOfUse",
                    "https://schema.hbp.eu/searchUi/termsOfUse"
                  )
                )
              )
            )
          ),
          Nested(
            "external_datalink",
            ObjectReader(
              "search:external_datalink",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "publications",
            ObjectReader(
              "search:publications",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "atlas",
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
            "preparation",
            ObjectReader(
              "search:preparation",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "modalityForFilter",
            ObjectReader(
              "search:modalityForFilter",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet")
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
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
                  PrimitiveToObjectWithCustomField[Int](
                    "searchUi:overviewMaxDisplay",
                    "https://schema.hbp.eu/searchUi/overviewMaxDisplay",
                    identity
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isFilterableFacet",
                    "https://schema.hbp.eu/searchUi/isFilterableFacet"
                  )
                )
              )
            )
          ),
          Nested(
            "protocol",
            ObjectReader(
              "search:protocols",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:tag_icon",
                    "https://schema.hbp.eu/searchUi/tag_icon"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
                  PrimitiveToObjectWithCustomField[Int](
                    "searchUi:overviewMaxDisplay",
                    "https://schema.hbp.eu/searchUi/overviewMaxDisplay",
                    identity
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isFilterableFacet",
                    "https://schema.hbp.eu/searchUi/isFilterableFacet"
                  )
                )
              )
            )
          ),
          Nested(
            "viewer",
            OrElse(
              ObjectReader(
                "search:brainviewer",
                WriteObject(
                  List(
                    PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                    PrimitiveToObjectWithCustomField[String](
                      "searchUi:link_icon",
                      "https://schema.hbp.eu/searchUi/link_icon"
                    )
                  )
                )
              ),
              ObjectReader(
                "search:neuroglancer",
                WriteObject(
                  List(
                    PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                    PrimitiveToObjectWithCustomField[String](
                      "searchUi:link_icon",
                      "https://schema.hbp.eu/searchUi/link_icon"
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "subjects",
            ObjectReader(
              "search:subjects",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isTable",
                    "https://schema.hbp.eu/searchUi/isTable"
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
                                  PrimitiveToObjectWithCustomField[Boolean](
                                    "searchUi:groupby",
                                    "https://schema.hbp.eu/searchUi/groupby",
                                    identity
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
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
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
                          Nested(
                            "samples",
                            ObjectReader(
                              "search:samples",
                              WriteObject(
                                List(
                                  PrimitiveToObjectWithCustomField[String](
                                    "label",
                                    "https://schema.hbp.eu/graphQuery/label"
                                  ),
                                  PrimitiveToObjectWithCustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort")
                                )
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
    "https://schema.hbp.eu/searchUi/ribbon" -> ObjectReader(
      "searchUi:ribbon",
      WriteObject(
        List(
          PrimitiveToObjectWithCustomField[String]("searchUi:content", "https://schema.hbp.eu/searchUi/content"),
          Nested(
            "https://schema.hbp.eu/searchUi/framed",
            ObjectReader(
              "searchUi:framed",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:aggregation",
                    "https://schema.hbp.eu/searchUi/aggregation"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:dataField",
                    "https://schema.hbp.eu/searchUi/dataField"
                  ),
                  Nested(
                    "https://schema.hbp.eu/searchUi/suffix",
                    ObjectReader(
                      "searchUi:suffix",
                      WriteObject(
                        List(
                          PrimitiveToObjectWithCustomField[String](
                            "searchUi:plural",
                            "https://schema.hbp.eu/searchUi/plural"
                          ),
                          PrimitiveToObjectWithCustomField[String](
                            "searchUi:singular",
                            "https://schema.hbp.eu/searchUi/singular"
                          ),
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          PrimitiveToObjectWithCustomField[String]("searchUi:icon", "https://schema.hbp.eu/searchUi/icon"),
        )
      )
    ),
    "https://schema.hbp.eu/searchUi/defaultSelection" -> Get[Boolean]("searchUi:defaultSelection"),
    "https://schema.hbp.eu/searchUi/boost"            -> Get[Double]("searchUi:boost"),
    "https://schema.hbp.eu/searchUi/icon"             -> Get[String]("searchUi:icon"),
    "http://schema.org/identifier"                    -> Get[String]("http://schema.org/identifier"),
    "https://schema.hbp.eu/searchUi/order"            -> Get[Int]("searchUi:order"),
    "http://schema.org/name"                          -> Get[String]("schema:name")
  )
}
