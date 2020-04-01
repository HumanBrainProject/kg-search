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
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch", identity),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible", identity)
        )
      )
    ),
    "title" -> ObjectReader(
      "search:title",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:optional", "https://schema.hbp.eu/searchUi/optional", identity),
          CustomField[Boolean]("searchUi:sort", "https://schema.hbp.eu/searchUi/sort", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost", identity)
        )
      )
    ),
    "contributors" -> ObjectReader(
      "search:contributors",
      ObjectValue(
        List(
          CustomField[String]("searchUi:separator", "https://schema.hbp.eu/searchUi/separator", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type", identity),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost", identity),
          CustomField[Boolean]("searchUi:label_hidden", "https://schema.hbp.eu/searchUi/label_hidden", identity)
        )
      )
    ),
    "zip" ->
    Merge(
      ObjectReader(
        "search:containerUrlAsZIP",
        ObjectValue(
          List(
            CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton", identity),
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity)
          )
        )
      ),
      ObjectReader(
        "search:files",
        ObjectValue(
          List(
            CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse", identity),
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
          CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton", identity),
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity)
        )
      )
    ),
    "dataDescriptor" -> ObjectReader(
      "search:dataDescriptorURL",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:isButton", "https://schema.hbp.eu/searchUi/isButton", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse", identity),
        )
      )
    ),
    "doi" -> ObjectReader(
      "search:doi",
      ObjectValue(
        List(
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity)
        )
      )
    ),
    "license_info" -> ObjectReader(
      "search:license",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type", identity),
          CustomField[String]("searchUi:facet_order", "https://schema.hbp.eu/searchUi/facet_order", identity)
        )
      )
    ),
    "component" -> ObjectReader(
      "search:component",
      ObjectValue(
        List(
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost", identity)
        )
      )
    ),
    "owners" -> ObjectReader(
      "search:owners",
      ObjectValue(
        List(
          CustomField[String]("searchUi:separator", "https://schema.hbp.eu/searchUi/separator", identity),
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost", identity)
        )
      )
    ),
    "description" -> ObjectReader(
      "search:description",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[Int]("searchUi:boost", "https://schema.hbp.eu/searchUi/boost", identity),
          CustomField[Boolean]("searchUi:label_hidden", "https://schema.hbp.eu/searchUi/label_hidden", identity)
        )
      )
    ),
    "speciesFilter" -> ObjectReader(
      "search:speciesFilter",
      ObjectValue(
        List(
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet", identity),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type", identity)
        )
      )
    ),
    "embargoForFilter" -> ObjectReader(
      "search:embargoForFilter",
      ObjectValue(
        List(
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet", identity),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity)
        )
      )
    ),
    "embargo" -> ObjectReader(
      "search:embargo",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity)
        )
      )
    ),
    "files" -> ObjectReader(
      "search:files",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[Boolean]("searchUi:isTable", "https://schema.hbp.eu/searchUi/isTable", identity),
          CustomField[Boolean]("searchUi:termsOfUse", "https://schema.hbp.eu/searchUi/termsOfUse", identity)
        )
      )
    ),
    "external_datalink" -> ObjectReader(
      "search:external_datalink",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity)
        )
      )
    ),
    "publications" -> ObjectReader(
      "search:publications",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:markdown", "https://schema.hbp.eu/searchUi/markdown", identity),
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity)
        )
      )
    ),
    "atlas" -> ObjectReader(
      "search:parcellationAtlas",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity)
        )
      )
    ),
    "region" -> ObjectReader(
      "search:parcellationRegion",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon", identity)
        )
      )
    ),
    "preparation" -> ObjectReader(
      "search:preparation",
      ObjectValue(
        List(
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity)
        )
      )
    ),
    "methods" -> ObjectReader(
      "search:methods",
      ObjectValue(
        List(
          CustomField[String]("searchUi:tag_icon", "https://schema.hbp.eu/searchUi/tag_icon", identity),
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet", identity),
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order", identity),
          CustomField[Int](
            "searchUi:overviewMaxDisplay",
            "https://schema.hbp.eu/searchUi/overviewMaxDisplay",
            identity
          ),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview", identity)
        )
      )
    ),
    "protocol" -> ObjectReader(
      "search:protocols",
      ObjectValue(
        List(
          CustomField[String]("searchUi:tag_icon", "https://schema.hbp.eu/searchUi/tag_icon", identity),
          CustomField[String]("searchUi:facet", "https://schema.hbp.eu/searchUi/facet", identity),
          CustomField[Int]("searchUi:order", "https://schema.hbp.eu/searchUi/order", identity),
          CustomField[Int](
            "searchUi:overviewMaxDisplay",
            "https://schema.hbp.eu/searchUi/overviewMaxDisplay",
            identity
          ),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[Boolean]("searchUi:overview", "https://schema.hbp.eu/searchUi/overview", identity)
        )
      )
    ),
    "viewer" -> OrElse(
      ObjectReader(
        "search:brainviewer",
        ObjectValue(
          List(
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
            CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon", identity)
          )
        )
      ),
      ObjectReader(
        "search:neuroglancer",
        ObjectValue(
          List(
            CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
            CustomField[String]("searchUi:link_icon", "https://schema.hbp.eu/searchUi/link_icon", identity)
          )
        )
      )
    ),
    "subjects" -> ObjectReader(
      "search:subjects",
      ObjectValue(
        List(
          CustomField[String]("searchUi:hint", "https://schema.hbp.eu/searchUi/hint", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:layout", "https://schema.hbp.eu/searchUi/layout", identity),
          CustomField[Boolean]("searchUi:isTable", "https://schema.hbp.eu/searchUi/isTable", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
                        CustomField[Boolean]("searchUi:groupby", "https://schema.hbp.eu/searchUi/groupby", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
                        CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
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
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch", identity),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type", identity)
        )
      )
    ),
    "last_release" -> ObjectReader(
      "search:lastReleaseAt",
      ObjectValue(
        List(
          CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch", identity),
          CustomField[Boolean]("searchUi:visible", "https://schema.hbp.eu/searchUi/visible", identity),
          CustomField[String]("label", "https://schema.hbp.eu/graphQuery/label", identity),
          CustomField[String]("searchUi:type", "https://schema.hbp.eu/searchUi/type", identity)
        )
      )
    ),
  )
}
