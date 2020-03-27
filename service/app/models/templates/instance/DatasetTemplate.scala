package models.templates.instance

import java.net.URLEncoder

import models.DatabaseScope
import models.templates.Template
import models.templates.entities.{CustomObject, NestedObject, ObjectValueMap, UrlObject, ValueObject, ValueObjectList}
import play.api.libs.json._
import utils._

trait DatasetTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val template = Map(
    "identifier" -> Value("identifier"),
    "title"      -> Value("title"),
    "contributors" -> ObjectListReader(
      "contributors",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          Value("name")
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
            case (Some(ValueObject(maybeCitation)), Some(ValueObject(maybeDoi))) =>
              val strOpt = for {
                citationStr <- maybeCitation
                doiStr      <- maybeDoi
              } yield citationStr + doiStr
              strOpt.map(s => ValueObject(Some(s)))
            case _ => None
          }
      }
    ),
    "zip"            -> Value("zip"),
    "dataDescriptor" -> Value("zip"),
    "doi"            -> FirstElement(ValueList("doi")),
    "license_info"   -> FirstElement(ObjectListReader("license", ObjectValue(List(Url("url"), Value("name"))))),
    "component" -> FirstElement(
      ObjectListReader(
        "component",
        ObjectValue(
          List(Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Project"))), Value("name"))
        )
      )
    ),
    "owners" -> FirstElement(
      ObjectListReader(
        "owners",
        ObjectValue(
          List(
            Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
            Value("name")
          )
        )
      )
    ),
    "description"      -> Value("description"),
    "speciesFilter"    -> FirstElement(ValueList("speciesFilter")),
    "embargoForFilter" -> FirstElement(ValueList("embargoForFilter")),
    "embargo" -> Optional(
      FirstElement(
        ValueList(
          "embargo",
          s =>
            s match {
              case ValueObject(Some("Embargoed")) =>
                ValueObject(
                  Some(
                    "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                  )
                )
              case ValueObject(Some("Under review")) =>
                ValueObject(
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
                ObjectValue(List(Url("absolute_path"), Value("name"))),
                Value("private_access"),
                urlOpt =>
                  privateAccesOpt => {
                    (urlOpt, privateAccesOpt) match {
                      case (
                          Some(ObjectValueMap(List(UrlObject(url), ValueObject(name)))),
                          Some(ValueObject(privateAccess))
                          ) =>
                        val opt = for {
                          privateAccessVal <- privateAccess
                          if privateAccessVal.toBoolean
                          urlStr  <- url
                          nameStr <- name
                        } yield
                          ObjectValueMap(
                            List(
                              UrlObject(Some(s"$fileProxy/files/cscs?url=$urlStr")),
                              ValueObject(Some(s"ACCESS PROTECTED: $nameStr"))
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
                        case (Some(ValueObject(preview)), Some(ValueObject(isAnimated))) =>
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
    "external_datalink" -> ObjectValue(List(Url("external_datalink"), Value("external_datalink"))),
    "publications" -> ObjectListReader(
      "publications",
      Merge(
        Value("citation"),
        Value("doi", doi => {
          doi.map { doiStr =>
            val url = URLEncoder.encode(doiStr, "UTF-8")
            s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
          }
        }),
        citation =>
          doi => {
            (citation, doi) match {
              case (Some(citationObj: ValueObject), Some(doiObj: ValueObject)) =>
                val strOpt = for {
                  citationStr <- citationObj.value
                  doiStr      <- doiObj.value
                } yield citationStr + "\n" + doiStr
                strOpt.map(str => ValueObject(Some(str)))
              case _ => doi
            }

        }
      )
    ),
    "atlas" -> FirstElement(ValueList("parcellationAtlas")),
    "region" -> ObjectListReader(
      "parcellationRegion",
      ObjectValue(List(Url("url"), OrElse(Value("alias"), Value("name"))))
    ),
    "preparation" -> FirstElement(ValueList("preparation")),
    "methods"     -> ValueList("methods"),
    "protocol"    -> ValueList("protocol"),
    "viewer" ->
    OrElse(
      ObjectListReader(
        "brainviewer",
        ObjectValue(List(Url("url"), Value("name", js => js.map(str => "Show " + str + " in brain atlas viewer"))))
      ),
      ObjectListReader(
        "neuroglancer",
        ObjectValue(List(Url("url"), Value("title", js => js.map(str => "Show " + str + " in brain atlas viewer"))))
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
                Value("name"),
              )
            )
          ),
          Nested("species", FirstElement(ValueList("species"))),
          Nested("sex", FirstElement(ValueList("sex"))),
          Nested("age", Value("age")),
          Nested("agecategory", FirstElement(ValueList("agecategory"))),
          Nested("weight", Value("weight")),
          Nested("strain", Optional(Value("strain"))),
          Nested("genotype", Value("genotype")),
          Nested(
            "samples",
            ObjectListReader(
              "samples",
              ObjectValue(
                List(Reference("identifier", ref => ref.map(TemplateHelper.refUUIDToSearchId("Sample"))), Value("name"))
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
    "first_release" -> Value("first_release"),
    "last_release"  -> Value("last_release"),
  )

}
