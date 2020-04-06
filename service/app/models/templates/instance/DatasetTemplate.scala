/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package models.templates.instance

import java.net.URLEncoder

import models.{DatabaseScope, INFERRED}
import models.templates.Template
import models.templates.entities.{ValueObject, _}
import utils._

import scala.collection.immutable.HashMap

trait DatasetTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> Value[String]("identifier"),
    "title"      -> Value[String]("title"),
    "contributors" -> ObjectListReader(
      "contributors",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          Value[String]("name")
        )
      )
    ),
    "citation" -> Merge(
      FirstElement(ValueList[String]("citation")),
      FirstElement(ValueList[String]("doi", doi => {
        doi.map(doiStr => {
          val url = URLEncoder.encode(doiStr, "UTF-8")
          s" [DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
        })
      })), {
        case (Some(ValueObject(Some(citationStr: String))), Some(ValueObject(Some(doiStr: String)))) =>
          Some(ValueObject[String](Some(citationStr + doiStr)))
        case _ => None
      }
    ),
    "zip"            -> Value[String]("zip"),
    "dataDescriptor" -> Optional(Value[String]("dataDescriptor")),
    "doi"            -> FirstElement(ValueList[String]("doi")),
    "license_info" -> FirstElement(
      ObjectListReader("license", ObjectValue(List(Url("url"), Value[String]("name"))))
    ),
    "component" -> FirstElement(
      ObjectListReader(
        "component",
        ObjectValue(
          List(
            Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Project"))),
            Value[String]("name")
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
            Value[String]("name")
          )
        )
      )
    ),
    "description"      -> Value[String]("description"),
    "speciesFilter"    -> FirstElement(ValueList[String]("speciesFilter")),
    "embargoForFilter" -> FirstElement(ValueList[String]("embargoForFilter")),
    "embargo" -> Optional(
      FirstElement(
        ValueList[String](
          "embargo", {
            case ValueObject(Some("Embargoed")) =>
              ValueObject[String](
                Some(
                  "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                )
              )
            case ValueObject(Some("Under review")) =>
              ValueObject[String](
                Some(
                  "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."
                )
              )
            case ValueObject(Some("Free")) =>
              ValueObject[String](
                Some(
                  "Free"
                )
              )
            case _ => ValueObject(None)
          }
        )
      )
    ),
    "files" -> Optional(
      Merge(
        FirstElement(ValueList[String]("embargo")),
        ObjectListReader(
          "files",
          ObjectValue(
            List(
              Merge(
                ObjectValue(List(Url("absolute_path"), Value[String]("name"))),
                Value[Boolean]("private_access"),
                (urlOpt, privateAccesOpt) => {
                  (urlOpt, privateAccesOpt) match {
                    case (
                        Some(ObjectValueMap(List(UrlObject(Some(urlStr)), ValueObject(Some(nameStr))))),
                        Some(ValueObject(Some(true)))
                        ) =>
                      Some(
                        ObjectValueMap(
                          List(
                            UrlObject(Some(s"$fileProxy/files/cscs?url=$urlStr")),
                            ValueObject[String](Some(s"ACCESS PROTECTED: $nameStr"))
                          )
                        )
                      )
                    case _ => urlOpt
                  }

                }
              ),
              CustomField[String]("human_readable_size", "fileSize"),
              Optional(
                Merge(
                  FirstElement(ValueList[String]("preview_url")),
                  FirstElement(ValueList[String]("is_preview_animated")), {
                    case (
                        Some(ValueObject(Some(preview: String))),
                        Some(ValueObject(Some(isAnimated: String)))
                        ) =>
                      Some(
                        NestedObject(
                          "previewUrl",
                          ObjectValueMap(
                            List(
                              UrlObject(Some(preview)),
                              CustomObject("isAnimated", Some(isAnimated.toBoolean.toString))
                            )
                          )
                        )
                      )

                    case _ => None
                  }
                )
              )
            )
          )
        ),
        (embargoOpt, filesOpt) => {
          embargoOpt.fold(filesOpt)(_ => None)
        }
      )
    ),
    "external_datalink" -> ObjectValue(List(Url("external_datalink"), Value[String]("external_datalink"))),
    "publications" -> ObjectListReader(
      "publications",
      Merge(
        Value[String]("citation"),
        Value[String]("doi", doi => {
          doi.map { doiStr =>
            val url = URLEncoder.encode(doiStr, "UTF-8")
            s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
          }
        }),
        (citation, doi) => {
          (citation, doi) match {
            case (Some(ValueObject(Some(citationStr: String))), Some(ValueObject(Some(doiStr: String)))) =>
              Some(ValueObject[String](Some(citationStr + "\n" + doiStr)))
            case _ => doi
          }

        }
      )
    ),
    "atlas" -> FirstElement(ValueList[String]("parcellationAtlas")),
    "region" -> ObjectListReader(
      "parcellationRegion",
      ObjectValue(List(Url("url"), OrElse(Value[String]("alias"), Value[String]("name"))))
    ),
    "preparation" -> FirstElement(ValueList[String]("preparation")),
    "methods"     -> ValueList[String]("methods"),
    "protocol"    -> ValueList[String]("protocol"),
    "viewer" ->
    OrElse(
      ObjectListReader(
        "brainviewer",
        ObjectValue(
          List(Url("url"), Value[String]("name", js => js.map(str => "Show " + str + " in brain atlas viewer")))
        )
      ),
      ObjectListReader(
        "neuroglancer",
        ObjectValue(
          List(Url("url"), Value[String]("title", js => js.map(str => "Show " + str + " in brain atlas viewer")))
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
                Value[String]("name"),
              )
            )
          ),
          Nested("species", FirstElement(ValueList[String]("species"))),
          Nested("sex", FirstElement(ValueList[String]("sex"))),
          Nested("age", Value[String]("age")),
          Nested("agecategory", FirstElement(ValueList[String]("agecategory"))),
          Nested("weight", Value[String]("weight")),
          Nested("strain", Optional(Value[String]("strain"))),
          Nested("genotype", Value[String]("genotype")),
          Nested(
            "samples",
            ObjectListReader(
              "samples",
              ObjectValue(
                List(
                  Reference("identifier", ref => ref.map(TemplateHelper.refUUIDToSearchId("Sample"))),
                  Value[String]("name")
                )
              )
            )
          )
        ), { a =>
          ObjectValueMap(List(NestedObject("children", a)))
        }
      )
    ),
    "first_release" -> Value[String]("first_release"),
    "last_release"  -> Value[String]("last_release"),
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> Value[String]("editorId")) ++ result
    case _        => result
  }
}
