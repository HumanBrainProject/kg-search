package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectMap
import utils._

trait PersonMetaTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "fields" -> ObjectReader(
      "fields",
      WriteObject(
        List(
          Nested(
            "identifier",
            ObjectReader(
              "search:identifier",
              PrimitiveToObjectWithCustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible")
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
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
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
            "phone",
            ObjectReader(
              "search:phone",
              PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            )
          ),
          Nested(
            "custodianOf",
            ObjectReader(
              "search:custodianOf",
              WriteObject(
                List(
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
            "custodianOfModel",
            ObjectReader(
              "search:custodianOfModel",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
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
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                )
              )
            )
          ),
          Nested(
            "address",
            ObjectReader(
              "search:address",
              PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            )
          ),
          Nested(
            "contributions",
            ObjectReader(
              "search:contributions",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  )
                )
              )
            )
          ),
          Nested(
            "modelContributions",
            ObjectReader(
              "search:modelContributions",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                )
              )
            )
          ),
          Nested(
            "email",
            ObjectReader(
              "search:email",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
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
    "https://schema.hbp.eu/searchUi/boost" -> Get[Double]("searchUi:boost"),
    "https://schema.hbp.eu/searchUi/icon"  -> Get[String]("searchUi:icon"),
    "http://schema.org/identifier"         -> Get[String]("http://schema.org/identifier"),
    "https://schema.hbp.eu/searchUi/order" -> Get[Int]("searchUi:order"),
    "http://schema.org/name"               -> Get[String]("schema:name")
  )
}
