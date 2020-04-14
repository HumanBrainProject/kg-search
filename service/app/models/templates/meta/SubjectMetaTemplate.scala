package models.templates.meta

import models.templates.Template
import play.api.libs.json.{JsArray, JsObject}
import utils._

trait SubjectMetaTemplate extends Template {

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
            "species",
            ObjectReader(
              "search:species",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
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
            "sex",
            ObjectReader(
              "search:sex",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
                  )
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
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
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
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
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
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
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
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
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
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:overview",
                    "https://schema.hbp.eu/searchUi/overview"
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
                    "searchUi:aggregate",
                    "https://schema.hbp.eu/searchUi/aggregate"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
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
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:visible",
                    "https://schema.hbp.eu/searchUi/visible"
                  ),
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
                  Nested(
                    "children",
                    WriteObject(
                      List(
                        Set("component", JsArray.empty),
                        Set("name", JsObject.empty)
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
