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
import models.templates.entities.{ObjectWithValueField, _}
import utils._

import scala.collection.immutable.HashMap

trait DatasetTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title"      -> PrimitiveToObjectWithValueField[String]("title"),
    "contributors" -> ObjectArrayToListOfObject(
      "contributors",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "relativeUrl",
            ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))
          ),
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "citation" -> Merge(
      FirstElement(PrimitiveArrayToListOfValueObject[String]("citation")),
      FirstElement(PrimitiveArrayToListOfValueObject[String]("doi", doi => {
        doi.map(doiStr => {
          val url = URLEncoder.encode(doiStr, "UTF-8")
          s" [DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
        })
      })), {
        case (
            Some(ObjectWithValueField(Some(citationStr: String))),
            Some(ObjectWithValueField(Some(doiStr: String)))
            ) =>
          Some(ObjectWithValueField[String](Some(citationStr + doiStr)))
        case _ => None
      }
    ),
    "zip" -> Optional(
      Merge(
        Merge(
          FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo")),
          PrimitiveToObjectWithValueField[Boolean]("containerUrlAsZip"), {
            case (
                embargoObject @ Some(ObjectWithValueField(Some(embargoString: String))),
                Some(ObjectWithValueField(Some(true)))
                ) if embargoString != "Under review" && embargoString != "Embargoed" =>
              embargoObject
            case _ => None
          }
        ),
        WriteObject(
          List(
            PrimitiveToObjectWithValueField[String]("container_url"),
            PrimitiveToObjectWithCustomField[String]("human_readable_size", "fileSize"),
          ),
        ), {
          case (Some(ObjectWithValueField(embargo)), containerUrlObject) =>
            containerUrlObject
          case _ => None
        }
      )
    ),
    "dataDescriptor" -> Optional(PrimitiveToObjectWithValueField[String]("dataDescriptor")),
    "doi"            -> FirstElement(PrimitiveArrayToListOfValueObject[String]("doi")),
    "license_info" -> FirstElement(
      ObjectArrayToListOfObject(
        "license",
        WriteObject(List(PrimitiveToObjectWithUrlField("url"), PrimitiveToObjectWithValueField[String]("name")))
      )
    ),
    "component" -> FirstElement(
      ObjectArrayToListOfObject(
        "component",
        WriteObject(
          List(
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(TemplateHelper.schemaIdToSearchId("Project"))
            ),
            PrimitiveToObjectWithValueField[String]("name")
          )
        )
      )
    ),
    "owners" -> FirstElement(
      ObjectArrayToListOfObject(
        "owners",
        WriteObject(
          List(
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))
            ),
            PrimitiveToObjectWithValueField[String]("name")
          )
        )
      )
    ),
    "description"      -> PrimitiveToObjectWithValueField[String]("description"),
    "speciesFilter"    -> FirstElement(PrimitiveArrayToListOfValueObject[String]("speciesFilter")),
    "embargoForFilter" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("embargoForFilter")),
    "embargo" -> Optional(
      FirstElement(
        PrimitiveArrayToListOfValueObject[String](
          "embargo", {
            case ObjectWithValueField(Some("Embargoed")) =>
              ObjectWithValueField[String](
                Some(
                  "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                )
              )
            case ObjectWithValueField(Some("Under review")) =>
              ObjectWithValueField[String](
                Some(
                  "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."
                )
              )
            case ObjectWithValueField(Some("Free")) =>
              ObjectWithValueField[String](
                Some(
                  "Free"
                )
              )
            case _ => ObjectWithValueField(None)
          }
        )
      )
    ),
    "files" -> Optional(
      Merge(
        FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo")),
        ObjectArrayToListOfObject(
          "files",
          WriteObject(
            List(
              Merge(
                WriteObject(
                  List(PrimitiveToObjectWithUrlField("absolute_path"), PrimitiveToObjectWithValueField[String]("name"))
                ),
                PrimitiveToObjectWithValueField[Boolean]("private_access"),
                (urlOpt, privateAccesOpt) => {
                  (urlOpt, privateAccesOpt) match {
                    case (
                        Some(ObjectMap(List(ObjectWithUrlField(Some(urlStr)), ObjectWithValueField(Some(nameStr))))),
                        Some(ObjectWithValueField(Some(true)))
                        ) =>
                      Some(
                        ObjectMap(
                          List(
                            ObjectWithUrlField(Some(s"$fileProxy/files/cscs?url=$urlStr")),
                            ObjectWithValueField[String](Some(s"ACCESS PROTECTED: $nameStr"))
                          )
                        )
                      )
                    case _ => urlOpt
                  }

                }
              ),
              PrimitiveToObjectWithCustomField[String]("human_readable_size", "fileSize"),
              Optional(
                Merge(
                  FirstElement(PrimitiveArrayToListOfValueObject[String]("preview_url")),
                  FirstElement(PrimitiveArrayToListOfValueObject[String]("is_preview_animated")), {
                    case (
                        Some(ObjectWithValueField(Some(preview: String))),
                        Some(ObjectWithValueField(Some(isAnimated: String)))
                        ) =>
                      Some(
                        NestedObject(
                          "previewUrl",
                          ObjectMap(
                            List(
                              ObjectWithUrlField(Some(preview)),
                              ObjectWithCustomField("isAnimated", Some(isAnimated.toBoolean.toString))
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
    "external_datalink" -> WriteObject(
      List(
        PrimitiveToObjectWithUrlField("external_datalink"),
        PrimitiveToObjectWithValueField[String]("external_datalink")
      )
    ),
    "publications" -> ObjectArrayToListOfObject(
      "publications",
      Merge(
        PrimitiveToObjectWithValueField[String]("citation"),
        PrimitiveToObjectWithValueField[String]("doi", doi => {
          doi.map { doiStr =>
            val url = URLEncoder.encode(doiStr, "UTF-8")
            s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
          }
        }),
        (citation, doi) => {
          (citation, doi) match {
            case (
                Some(ObjectWithValueField(Some(citationStr: String))),
                Some(ObjectWithValueField(Some(doiStr: String)))
                ) =>
              Some(ObjectWithValueField[String](Some(citationStr + "\n" + doiStr)))
            case _ => doi
          }

        }
      )
    ),
    "atlas" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("parcellationAtlas")),
    "region" -> ObjectArrayToListOfObject(
      "parcellationRegion",
      WriteObject(
        List(
          PrimitiveToObjectWithUrlField("url"),
          OrElse(PrimitiveToObjectWithValueField[String]("alias"), PrimitiveToObjectWithValueField[String]("name"))
        )
      )
    ),
    "preparation" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("preparation")),
    "methods"     -> PrimitiveArrayToListOfValueObject[String]("methods"),
    "protocol"    -> PrimitiveArrayToListOfValueObject[String]("protocol"),
    "viewer" ->
    OrElse(
      ObjectArrayToListOfObject(
        "brainviewer",
        WriteObject(
          List(
            PrimitiveToObjectWithUrlField("url"),
            PrimitiveToObjectWithValueField[String](
              "name",
              js => js.map(str => "Show " + str + " in brain atlas viewer")
            )
          )
        )
      ),
      ObjectArrayToListOfObject(
        "neuroglancer",
        WriteObject(
          List(
            PrimitiveToObjectWithUrlField("url"),
            PrimitiveToObjectWithValueField[String](
              "title",
              js => js.map(str => "Show " + str + " in brain atlas viewer")
            )
          )
        )
      )
    ),
    "subjects" ->
    ObjectArrayToListOfObject(
      "subjects",
      Nested(
        "children",
        WriteObject(
          List(
            Nested(
              "subject_name",
              WriteObject(
                List(
                  PrimitiveToObjectWithReferenceField(
                    "identifier",
                    ref => ref.map(TemplateHelper.refUUIDToSearchId("Subject"))
                  ),
                  PrimitiveToObjectWithValueField[String]("name"),
                )
              )
            ),
            Nested("species", FirstElement(PrimitiveArrayToListOfValueObject[String]("species"))),
            Nested("sex", FirstElement(PrimitiveArrayToListOfValueObject[String]("sex"))),
            Nested("age", PrimitiveToObjectWithValueField[String]("age")),
            Nested("agecategory", FirstElement(PrimitiveArrayToListOfValueObject[String]("agecategory"))),
            Nested("weight", PrimitiveToObjectWithValueField[String]("weight")),
            Nested("strain", Optional(PrimitiveToObjectWithValueField[String]("strain"))),
            Nested("genotype", PrimitiveToObjectWithValueField[String]("genotype")),
            Nested(
              "samples",
              ObjectArrayToListOfObject(
                "samples",
                WriteObject(
                  List(
                    PrimitiveToObjectWithReferenceField(
                      "identifier",
                      ref => ref.map(TemplateHelper.refUUIDToSearchId("Sample"))
                    ),
                    PrimitiveToObjectWithValueField[String]("name")
                  )
                )
              )
            )
          ),
        )
      )
    ),
    "first_release" -> PrimitiveToObjectWithValueField[String]("first_release"),
    "last_release"  -> PrimitiveToObjectWithValueField[String]("last_release"),
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _        => result
  }
}
