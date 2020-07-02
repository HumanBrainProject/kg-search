package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectMap
import utils._

trait ModelInstanceMetaTemplate extends Template {

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
            "description",
            ObjectReader(
              "search:description",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
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
            "version",
            ObjectReader(
              "search:version",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "owners",
            ObjectReader(
              "search:custodian",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:type",
                    "https://schema.hbp.eu/searchUi/type"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:hint",
                    "https://schema.hbp.eu/searchUi/hint"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "mainContact",
            ObjectReader(
              "search:mainContact",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:type",
                    "https://schema.hbp.eu/searchUi/type"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
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
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:type",
                    "https://schema.hbp.eu/searchUi/type"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:label_hidden",
                    "https://schema.hbp.eu/searchUi/label_hidden"
                  ),
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
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
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
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
            "embargo",
            ObjectReader(
              "search:embargo",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  )
                )
              )
            )
          ),
          Nested(
            "allfiles",
            ObjectReader(
              "search:fileBundle",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:termsOfUse",
                    "https://schema.hbp.eu/searchUi/termsOfUse"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:isButton",
                    "https://schema.hbp.eu/searchUi/isButton"
                  )
                )
              )
            )
          ),
          Nested(
            "brainStructures",
            ObjectReader(
              "search:brainStructure",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "cellularTarget",
            ObjectReader(
              "search:cellularTarget",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "studyTarget",
            ObjectReader(
              "search:studyTarget",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                )
              )
            )
          ),
          Nested(
            "modelScope",
            ObjectReader(
              "search:modelScope",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:layout",
                    "https://schema.hbp.eu/searchUi/layout"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "abstractionLevel",
            ObjectReader(
              "search:abstractionLevel",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "modelFormat",
            ObjectReader(
              "search:modelFormat",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String](
                    "searchUi:separator",
                    "https://schema.hbp.eu/searchUi/separator"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
                )
              )
            )
          ),
          Nested(
            "usedDataset",
            ObjectReader(
              "search:usedDataset",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
                )
              )
            )
          ),
          Nested(
            "producedDataset",
            ObjectReader(
              "search:producedDataset",
              WriteObject(
                List(
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
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
    "https://schema.hbp.eu/searchUi/boost" -> Get[Double]("searchUi:boost"),
    "https://schema.hbp.eu/searchUi/icon"  -> Get[String]("searchUi:icon"),
    "http://schema.org/identifier"         -> Get[String]("http://schema.org/identifier"),
    "https://schema.hbp.eu/searchUi/order" -> Get[Int]("searchUi:order"),
    "http://schema.org/name"               -> Get[String]("schema:name")
  )
}
