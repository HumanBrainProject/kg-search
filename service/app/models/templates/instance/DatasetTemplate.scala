package models.templates.instance

import java.net.URLEncoder

import models.DatabaseScope
import models.templates.Template
import models.templates.entities.{
  CustomObject,
  NestedObject,
  ObjectValueMap,
  UrlObject,
  ValueObjectBoolean,
  ValueObjectList,
  ValueObjectString
}
import play.api.libs.json._
import utils._

trait DatasetTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val template = Map(
    "identifier" -> ValueString("identifier"),
    "title"      -> ValueString("title"),
    "contributors" -> ObjectListReader(
      "contributors",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          ValueString("name")
        )
      )
    ),
    "citation" -> Merge(
      FirstElement(ValueList("citation")),
      FirstElement(ValueList("doi", doi => {
        doi.map(doiStr => {
          val url = URLEncoder.encode(doiStr, "UTF-8")
          s" [DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
        })
      })),
      l =>
        r => {
          (l, r) match {
            case (Some(ValueObjectString(maybeCitation)), Some(ValueObjectString(maybeDoi))) =>
              val strOpt = for {
                citationStr <- maybeCitation
                doiStr      <- maybeDoi
              } yield citationStr + doiStr
              strOpt.map(s => ValueObjectString(Some(s)))
            case _ => None
          }
      }
    ),
    "zip"            -> ValueString("zip"),
    "dataDescriptor" -> ValueString("zip"),
    "doi"            -> FirstElement(ValueList("doi")),
    "license_info"   -> FirstElement(ObjectListReader("license", ObjectValue(List(Url("url"), ValueString("name"))))),
    "component" -> FirstElement(
      ObjectListReader(
        "component",
        ObjectValue(
          List(
            Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Project"))),
            ValueString("name")
          )
        )
      )
    ),
    "owners" -> FirstElement(
      ObjectListReader(
        "owners",
        ObjectValue(
          List(
            Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
            ValueString("name")
          )
        )
      )
    ),
    "description"      -> ValueString("description"),
    "speciesFilter"    -> FirstElement(ValueList("speciesFilter")),
    "embargoForFilter" -> FirstElement(ValueList("embargoForFilter")),
    "embargo" -> Optional(
      FirstElement(
        ValueList(
          "embargo",
          s =>
            s match {
              case ValueObjectString(Some("Embargoed")) =>
                ValueObjectString(
                  Some(
                    "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                  )
                )
              case ValueObjectString(Some("Under review")) =>
                ValueObjectString(
                  Some(
                    "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."
                  )
                )
          }
        )
      )
    ),
    "files" -> Optional(
      Merge(
        FirstElement(ValueList("embargo")),
        ObjectListReader(
          "files",
          ObjectValue(
            List(
              Merge(
                ObjectValue(List(Url("absolute_path"), ValueString("name"))),
                ValueBoolean("private_access"),
                urlOpt =>
                  privateAccesOpt => {
                    (urlOpt, privateAccesOpt) match {
                      case (
                          Some(ObjectValueMap(List(UrlObject(url), ValueObjectString(name)))),
                          Some(ValueObjectBoolean(privateAccess))
                          ) =>
                        val opt = for {
                          privateAccessVal <- privateAccess
                          if privateAccessVal
                          urlStr  <- url
                          nameStr <- name
                        } yield
                          ObjectValueMap(
                            List(
                              UrlObject(Some(s"$fileProxy/files/cscs?url=$urlStr")),
                              ValueObjectString(Some(s"ACCESS PROTECTED: $nameStr"))
                            )
                          )
                        opt.orElse(urlOpt)
                      case _ => urlOpt
                    }

                }
              ),
              CustomField("human_readable_size", "fileSize"),
              Optional(
                Merge(
                  FirstElement(ValueList("preview_url")),
                  FirstElement(ValueList("is_preview_animated")),
                  previewOpt =>
                    isAnimatedOpt => {
                      (previewOpt, isAnimatedOpt) match {
                        case (Some(ValueObjectString(preview)), Some(ValueObjectString(isAnimated))) =>
                          for {
                            previewVal    <- preview
                            isAnimatedStr <- isAnimated
                          } yield
                            NestedObject(
                              "previewUrl",
                              ObjectValueMap(
                                List(
                                  UrlObject(Some(previewVal)),
                                  CustomObject("isAnimated", Some(isAnimatedStr.toBoolean.toString))
                                )
                              )
                            )

                        case _ => None
                      }

                  }
                )
              )
            )
          )
        ),
        embargoOpt =>
          filesOpt => {
            embargoOpt.fold(filesOpt)(_ => None)
        }
      )
    ),
    "external_datalink" -> ObjectValue(List(Url("external_datalink"), ValueString("external_datalink"))),
    "publications" -> ObjectListReader(
      "publications",
      Merge(
        ValueString("citation"),
        ValueString("doi", doi => {
          doi.map { doiStr =>
            val url = URLEncoder.encode(doiStr, "UTF-8")
            s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
          }
        }),
        citation =>
          doi => {
            (citation, doi) match {
              case (Some(citationObj: ValueObjectString), Some(doiObj: ValueObjectString)) =>
                val strOpt = for {
                  citationStr <- citationObj.value
                  doiStr      <- doiObj.value
                } yield citationStr + "\n" + doiStr
                strOpt.map(str => ValueObjectString(Some(str)))
              case _ => doi
            }

        }
      )
    ),
    "atlas" -> FirstElement(ValueList("parcellationAtlas")),
    "region" -> ObjectListReader(
      "parcellationRegion",
      ObjectValue(List(Url("url"), OrElse(ValueString("alias"), ValueString("name"))))
    ),
    "preparation" -> FirstElement(ValueList("preparation")),
    "methods"     -> ValueList("methods"),
    "protocol"    -> ValueList("protocol"),
    "viewer" ->
    OrElse(
      ObjectListReader(
        "brainviewer",
        ObjectValue(
          List(Url("url"), ValueString("name", js => js.map(str => "Show " + str + " in brain atlas viewer")))
        )
      ),
      ObjectListReader(
        "neuroglancer",
        ObjectValue(
          List(Url("url"), ValueString("title", js => js.map(str => "Show " + str + " in brain atlas viewer")))
        )
      )
    ),
    "subjects" -> ObjectListReader(
      "subjects",
      ObjectValue(
        List(
          Nested(
            "subject_name",
            ObjectValue(
              List(
                Reference("identifier", ref => ref.map(TemplateHelper.refUUIDToSearchId("Subject"))),
                ValueString("name"),
              )
            )
          ),
          Nested("species", FirstElement(ValueList("species"))),
          Nested("sex", FirstElement(ValueList("sex"))),
          Nested("age", ValueString("age")),
          Nested("agecategory", FirstElement(ValueList("agecategory"))),
          Nested("weight", ValueString("weight")),
          Nested("strain", Optional(ValueString("strain"))),
          Nested("genotype", ValueString("genotype")),
          Nested(
            "samples",
            ObjectListReader(
              "samples",
              ObjectValue(
                List(
                  Reference("identifier", ref => ref.map(TemplateHelper.refUUIDToSearchId("Sample"))),
                  ValueString("name")
                )
              )
            )
          )
        ),
        objectValue =>
          objectValue match {
            case a: ObjectValueMap => a.map(t => NestedObject("children", t))
        }
      )
    ),
    "first_release" -> ValueString("first_release"),
    "last_release"  -> ValueString("last_release"),
  )

}
