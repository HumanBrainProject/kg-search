package models.templates.meta

import models.templates.Template
import models.templates.entities.{NestedObject, ObjectValueMap}
import utils._

trait DatasetMetaTemplate extends Template {

  val template = Map(
    "identifier" -> ObjectReader(
      "search:identifier",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch"),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible")
        )
      )
    ),
    "title" -> ObjectReader(
      "search:title",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:optional", "https://schema.hbp.eu/searchUi/optional"),
          CustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
        )
      )
    ),
    "contributors" -> ObjectReader(
      "search:contributors",
      ObjectValue(
        List(
          CustomField[String]("searchUi:separator", "https://schema.hbp.eu/searchUi/separator"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
          CustomField[Boolean]("searchUi:label_hidden", "https://schema.hbp.eu/searchUi/label_hidden")
        )
      )
    ),
    "zip" ->
    Merge(
      ObjectReader(
        "search:containerUrlAsZIP",
        ObjectValue(
          List(
            CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton"),
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
          )
        )
      ),
      ObjectReader(
        "search:files",
        ObjectValue(
          List(
            CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse"),
          )
        )
      ),
      maybeZip =>
        maybeFiles =>
          (maybeZip, maybeFiles) match {
            case (Some(ObjectValueMap(zip)), Some(ObjectValueMap(List(termOfUse)))) =>
              Some(ObjectValueMap(zip :+ termOfUse))
            case _ => None
      }
    ),
    "citation" -> ObjectReader(
      "search:citation",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton"),
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
        )
      )
    ),
    "dataDescriptor" -> ObjectReader(
      "search:dataDescriptorURL",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse"),
        )
      )
    ),
    "doi" -> ObjectReader(
      "search:doi",
      ObjectValue(
        List(
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
        )
      )
    ),
    "license_info" -> ObjectReader(
      "search:license",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type"),
          CustomField[String]("searchUi:facet_order", "https://schema.hbp.eu/searchUi/facet_order")
        )
      )
    ),
    "component" -> ObjectReader(
      "search:component",
      ObjectValue(
        List(
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
        )
      )
    ),
    "owners" -> ObjectReader(
      "search:owners",
      ObjectValue(
        List(
          CustomField[String]("searchUi:separator", "https://schema.hbp.eu/searchUi/separator"),
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost")
        )
      )
    ),
    "description" -> ObjectReader(
      "search:description",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost"),
          CustomField[Boolean]("searchUi:label_hidden", "https://schema.hbp.eu/searchUi/label_hidden")
        )
      )
    ),
    "speciesFilter" -> ObjectReader(
      "search:speciesFilter",
      ObjectValue(
        List(
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
        )
      )
    ),
    "embargoForFilter" -> ObjectReader(
      "search:embargoForFilter",
      ObjectValue(
        List(
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label")
        )
      )
    ),
    "embargo" -> ObjectReader(
      "search:embargo",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
        )
      )
    ),
    "files" -> ObjectReader(
      "search:files",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[Boolean]("searchUi:isTable", "https://schema.hbp.eu/searchUi/isTable"),
          CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse")
        )
      )
    ),
    "external_datalink" -> ObjectReader(
      "search:external_datalink",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
        )
      )
    ),
    "publications" -> ObjectReader(
      "search:publications",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown"),
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
        )
      )
    ),
    "atlas" -> ObjectReader(
      "search:parcellationAtlas",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
        )
      )
    ),
    "region" -> ObjectReader(
      "search:parcellationRegion",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon")
        )
      )
    ),
    "preparation" -> ObjectReader(
      "search:preparation",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout")
        )
      )
    ),
    "methods" -> ObjectReader(
      "search:methods",
      ObjectValue(
        List(
          CustomField[String]("searchUi:tag_icon", "https://schema.hbp.eu/searchUi/tag_icon"),
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
          CustomField[Int]("searchUi:overviewMaxDisplay", "https://schema.hbp.eu/searchUi/overviewMaxDisplay"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview")
        )
      )
    ),
    "protocol" -> ObjectReader(
      "search:protocols",
      ObjectValue(
        List(
          CustomField[String]("searchUi:tag_icon", "https://schema.hbp.eu/searchUi/tag_icon"),
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet"),
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order"),
          CustomField[Int]("searchUi:overviewMaxDisplay", "https://schema.hbp.eu/searchUi/overviewMaxDisplay"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview")
        )
      )
    ),
    "viewer" -> OrElse(
      ObjectReader(
        "search:brainviewer",
        ObjectValue(
          List(
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon")
          )
        )
      ),
      ObjectReader(
        "search:neuroglancer",
        ObjectValue(
          List(
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
            CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon")
          )
        )
      )
    ),
    "subjects" -> ObjectReader(
      "search:subjects",
      ObjectValue(
        List(
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout"),
          CustomField[Boolean]("searchUi:isTable", "https://schema.hbp.eu/searchUi/isTable"),
          ObjectReader(
            "fields",
            ObjectValue(
              List(
                Nested(
                  "subject_name",
                  ObjectReader(
                    "search:name",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                        CustomField[Boolean]("searchUi:groupby", "https://schema.hbp.eu/searchUi/groupby"),
                      )
                    )
                  )
                ),
                Nested(
                  "species",
                  ObjectReader(
                    "search:species",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "sex",
                  ObjectReader(
                    "search:sex",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "age",
                  ObjectReader(
                    "search:age",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "agecategory",
                  ObjectReader(
                    "search:agecategory",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "weight",
                  ObjectReader(
                    "search:weight",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "strain",
                  ObjectReader(
                    "search:strain",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "genotype",
                  ObjectReader(
                    "search:genotype",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                ),
                Nested(
                  "samples",
                  ObjectReader(
                    "search:samples",
                    ObjectValue(
                      List(
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
                      )
                    )
                  )
                )
              ), { a =>
                ObjectValueMap(List(NestedObject("children", a)))
              }
            )
          )
        )
      )
    ),
    "first_release" -> ObjectReader(
      "search:firstReleaseAt",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch"),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
        )
      )
    ),
    "last_release" -> ObjectReader(
      "search:lastReleaseAt",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch"),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible"),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label"),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type")
        )
      )
    ),
  )
}
