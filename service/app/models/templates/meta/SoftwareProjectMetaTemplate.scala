package models.templates.meta

import models.templates.Template
import models.templates.entities.ObjectMap
import play.api.libs.json.JsString
import utils._

trait SoftwareProjectMetaTemplate extends Template {

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
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:label_hidden",
                    "https://schema.hbp.eu/searchUi/label_hidden"
                  ),
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                  PrimitiveToObjectWithCustomField[Boolean](
                    "searchUi:markdown",
                    "https://schema.hbp.eu/searchUi/markdown"
                  ),
                  PrimitiveToObjectWithCustomField[Double]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
                )
              )
            )
          ),
          Nested(
            "license",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:license",
                  PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                )
              )
            )
          ),
          Nested(
            "version",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:version",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "appCategory",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:applicationCategory",
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
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "operatingSystem",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:operatingSystem",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:tag_icon",
                        "https://schema.hbp.eu/searchUi/tag_icon"
                      ),
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:facet",
                        "https://schema.hbp.eu/searchUi/facet"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "homepage",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:homepage",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "sourceCode",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:sourceCode",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "documentation",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:documentation",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
                    )
                  )
                )
              )
            )
          ),
          Nested(
            "features",
            ObjectReader(
              "search:versions",
              ObjectReader(
                "fields",
                ObjectReader(
                  "search:features",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:tag_icon",
                        "https://schema.hbp.eu/searchUi/tag_icon"
                      ),
                      PrimitiveToObjectWithCustomField[String](
                        "searchUi:layout",
                        "https://schema.hbp.eu/searchUi/layout"
                      ),
                      PrimitiveToObjectWithCustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
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
    "http://schema.org/identifier"         -> Get[String]("http://schema.org/identifier"),
    "http://schema.org/name"               -> Get[String]("schema:name"),
    "https://schema.hbp.eu/searchUi/order" -> Get[Int]("searchUi:order"),
  )
}
