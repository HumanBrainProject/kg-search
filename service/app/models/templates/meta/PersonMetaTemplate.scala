package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectValueMap
import utils._

trait PersonMetaTemplate extends Template {

  val template: Map[String, TemplateComponent] = Map(
    "fields" -> ObjectReader(
      "fields",
      ObjectValue(
        List(
          Nested(
            "identifier",
            ObjectReader(
              "search:identifier",
              CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible")
            )
          ),
          Nested(
            "title",
            ObjectReader(
              "search:title",
              ObjectValue(
                List(
                  CustomField[Boolean]("searchUi:optional", "https://schema.hbp.eu/searchUi/optional"),
                  CustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
                )
              )
            )
          ),
          Nested(
            "description",
            ObjectReader(
              "search:description",
              ObjectValue(
                List(
                  CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                  CustomField[Boolean]("searchUi:label_hidden", "https://schema.hbp.eu/searchUi/label_hidden")
                )
              )
            )
          ),
          Nested(
            "phone",
            ObjectReader(
              "search:phone",
              CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            )
          ),
          Nested(
            "custodianOf",
            ObjectReader(
              "search:custodianOf",
              ObjectValue(
                List(
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview")
                )
              )
            )
          ),
          Nested(
            "custodianOfModel",
            ObjectReader(
              "search:custodianOfModel",
              ObjectValue(
                List(
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                )
              )
            )
          ),
          Nested(
            "publications",
            ObjectReader(
              "search:publications",
              ObjectValue(
                List(
                  CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  CustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                )
              )
            )
          ),
          Nested(
            "address",
            ObjectReader(
              "search:address",
              CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            )
          ),
          Nested(
            "contributions",
            ObjectReader(
              "search:contributions",
              ObjectValue(
                List(
                  CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview")
                )
              )
            )
          ),
          Nested(
            "modelContributions",
            ObjectReader(
              "search:modelContributions",
              ObjectValue(
                List(
                  CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                )
              )
            )
          ),
          Nested(
            "email",
            ObjectReader(
              "search:email",
              ObjectValue(
                List(
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
                  CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview")
                )
              )
            )
          ),
          Nested(
            "first_release",
            ObjectReader(
              "search:firstReleaseAt",
              ObjectValue(
                List(
                  CustomField[Boolean](
                    "searchUi:ignoreForSearch",
                    "https://schema.hbp.eu/searchUi/ignoreForSearch",
                    identity
                  ),
                  CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
                )
              )
            )
          ),
          Nested(
            "last_release",
            ObjectReader(
              "search:lastReleaseAt",
              ObjectValue(
                List(
                  CustomField[Boolean](
                    "searchUi:ignoreForSearch",
                    "https://schema.hbp.eu/searchUi/ignoreForSearch",
                    identity
                  ),
                  CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
                  CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
                )
              )
            )
          )
        )
      )
    ),
    "https://schema.hbp.eu/searchUi/ribbon" -> ObjectReader(
      "searchUi:ribbon",
      ObjectValue(
        List(
          CustomField[String]("searchUi:content", "https://schema.hbp.eu/searchUi/content"),
          Nested(
            "https://schema.hbp.eu/searchUi/framed",
            ObjectReader(
              "searchUi:framed",
              ObjectValue(
                List(
                  CustomField[String]("searchUi:aggregation", "https://schema.hbp.eu/searchUi/aggregation"),
                  CustomField[String]("searchUi:dataField", "https://schema.hbp.eu/searchUi/dataField"),
                  Nested(
                    "https://schema.hbp.eu/searchUi/suffix",
                    ObjectReader(
                      "searchUi:suffix",
                      ObjectValue(
                        List(
                          CustomField[String]("searchUi:plural", "https://schema.hbp.eu/searchUi/plural"),
                          CustomField[String]("searchUi:singular", "https://schema.hbp.eu/searchUi/singular"),
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          CustomField[String]("searchUi:icon", "https://schema.hbp.eu/searchUi/icon"),
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
