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

import models.{INFERRED, RELEASED}
import models.templates.{FileProxy, Template}
import models.templates.entities.{ObjectWithValueField, _}
import utils._

import scala.collection.immutable.HashMap

trait DatasetTemplate extends Template with FileProxy {

  val result: Map[String, TemplateComponent] = Map(
    "identifier" -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title" -> PrimitiveToObjectWithValueField[String]("title"),
    "contributors" -> ObjectArrayToListOfObject(
      "contributors",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "identifier",
            ref => ref.map(s => s"Contributor/$s")
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
              embargoObject@Some(ObjectWithValueField(Some(embargoString: String))),
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
    "doi" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("doi")),
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
              "identifier",
              ref => ref.map(s => s"Project/$s")
            ),
            PrimitiveToObjectWithValueField[String]("name")
          )
        )
      )
    ),
    "owners" ->
      ObjectArrayToListOfObject(
        "owners",
        WriteObject(
          List(
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Contributor/$s")
            ),
            PrimitiveToObjectWithValueField[String]("name")
          )
        )
      ),
    "description" -> PrimitiveToObjectWithValueField[String]("description"),
    "speciesFilter" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("speciesFilter")),
    "embargoForFilter" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("embargoForFilter")),
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
        PrimitiveToObjectWithValueField[String](
          "doi", {
            case Some(ObjectWithValueField(Some(doiStr))) =>
              val url = URLEncoder.encode(doiStr, "UTF-8")
              Some(ObjectWithValueField(Some(s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url")))
            case doi => doi
          }
        ),
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
//    "modalityForFilter" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("modalityForFilter")),
    "methods" -> PrimitiveArrayToListOfValueObject[String]("methods"),
    "protocol" -> PrimitiveArrayToListOfValueObject[String]("protocols"),
    "viewer" ->
      OrElse(
        ObjectArrayToListOfObject(
          "brainviewer",
          WriteObject(
            List(
              PrimitiveToObjectWithUrlField("url"),
              PrimitiveToObjectWithValueField[String](
                "name", {
                  case Some(ObjectWithValueField(Some(str))) =>
                    Some(ObjectWithValueField(Some("Show " + str + " in brain atlas viewer")))
                  case _ => Some(ObjectWithValueField(Some("Show in brain atlas viewer")))
                }
              )
            )
          )
        ),
        ObjectArrayToListOfObject(
          "neuroglancer",
          WriteObject(
            List(
              PrimitiveToObjectWithUrlField("url"),
              OrElse(
                PrimitiveToObjectWithValueField[String](
                  "name", {
                    case Some(ObjectWithValueField(Some(str))) =>
                      Some(ObjectWithValueField(Some("Show " + str + " in brain atlas viewer")))
                    case _ => None
                  }
                ),
                PrimitiveToObjectWithValueField[String](
                  "title", {
                    case Some(ObjectWithValueField(Some(str))) =>
                      Some(ObjectWithValueField(Some("Show " + str + " in brain atlas viewer")))
                    case _ => Some(ObjectWithValueField(Some("Show in brain atlas viewer")))
                  }
                )
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
                      ref => ref.map(s => s"Subject/$s")
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
              Nested(
                "strain",
                Optional(
                  OrElse(
                    PrimitiveToObjectWithValueField[String]("strain"),
                    PrimitiveToObjectWithValueField[String]("strains")
                  )
                )
              ),
              Nested("genotype", PrimitiveToObjectWithValueField[String]("genotype")),
              Nested(
                "samples",
                ObjectArrayToListOfObject(
                  "samples",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithReferenceField(
                        "identifier",
                        ref => ref.map(s => s"Sample/$s")
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
    "last_release" -> PrimitiveToObjectWithValueField[String]("last_release"),
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap(
      "editorId" -> PrimitiveToObjectWithValueField[String]("editorId"),
      "embargoRestrictedAccess" -> Merge(
        PrimitiveToObjectWithValueField[String]("container_url"),
        Optional(
          FirstElement(
            PrimitiveArrayToListOfValueObject[String](
              "embargo"
            )
          )
        ),
        (container_url, embargo) => {
          (container_url, embargo) match {
            case (
              Some(ObjectWithValueField(Some(url: String))),
              Some(ObjectWithValueField(Some("Embargoed")))
              ) if (url.startsWith("https://object.cscs.ch")) =>
              Some(
                ObjectWithValueField[String](
                  Some(
                    "This dataset is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=" + url + "\" target=\"_blank\"> you should be able to access the data here</a>"
                  )
                )
              )
            case (
              Some(ObjectWithValueField(Some(url: String))),
              Some(ObjectWithValueField(Some("Under review")))
              ) if (url.startsWith("https://object.cscs.ch")) =>
              Some(
                ObjectWithValueField[String](
                  Some(
                    "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=" + url + "\" target=\"_blank\"> you should be able to access the data here</a>"
                  )
                )
              )
            case _ =>
              None
          }
        }
      ),
      "files" -> Optional(
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
        )
      )
    ) ++ result
    case RELEASED => HashMap(
      "embargo" -> Optional(
        FirstElement(
          PrimitiveArrayToListOfValueObject[String](
            "embargo"
          ), {
            case Some(ObjectWithValueField(Some("Embargoed"))) =>
              Some(
                ObjectWithValueField[String](
                  Some(
                    "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                  )
                )
              )
            case Some(ObjectWithValueField(Some("Under review"))) =>
              Some(
                ObjectWithValueField[String](
                  Some(
                    "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."
                  )
                )
              )
            case _ =>
              None
          }
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
          ), {
            case (
              Some(ObjectWithValueField(Some(embargoString: String))),
              filesOpt
              ) if (embargoString != "Under review" && embargoString != "Embargoed") =>
              filesOpt
            case (None, filesOpt) => filesOpt
            case _ => None
          }
        )
      )
    ) ++ result
  }
}
