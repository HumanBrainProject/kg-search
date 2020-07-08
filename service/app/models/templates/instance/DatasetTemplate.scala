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
import play.api.libs.json.JsObject
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
        WriteObject(
          List(
            Nested("embargo", FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo"))),
            Nested("containerUrlAsZIP", PrimitiveToObjectWithValueField[Boolean]("containerUrlAsZIP")),
            Nested("files", ObjectArrayToListOfObject(
              "files",
              WriteObject(
                List(
                  PrimitiveToObjectWithUrlField("absolute_path")
                )
              )
            )
            )
          )
        ),
        PrimitiveToObjectWithUrlField("container_url", {
          case ObjectWithUrlField(Some(url)) =>
            ObjectWithUrlField(Some(s"https://kg.ebrains.eu/proxy/export?container=$url"))
          case _ => ObjectWithUrlField(None)
        }),
        {
          case (
            Some(visibilityCriteria),
            Some(ObjectWithUrlField(Some(res: String)))
            ) =>
            val embargo = (visibilityCriteria.toJson \ "embargo" \ "value").asOpt[String]
            val isEmbargoed = embargo match {
              case Some(em) => em == "Under review" || em == "Embargoed"
              case _ => false
            }
            val asZip = (visibilityCriteria.toJson \ "containerUrlAsZIP" \ "value").asOpt[Boolean].getOrElse(false)
            val hasFiles = (visibilityCriteria.toJson \ "files").asOpt[List[JsObject]].getOrElse(List()).nonEmpty
            if (!isEmbargoed && (!res.isBlank && (asZip || !hasFiles))) {
              Some(
                ObjectMap(
                  List(
                    ObjectWithUrlField(Some(res)),
                    ObjectWithValueField[String](Some("Download all related data as ZIP"))
                  )
                )
              )
            } else {
              None
            }
          case _ => None
        }
      )
    ),
    "dataDescriptor" -> Optional(
      WriteObject(
        List(
          PrimitiveToObjectWithUrlField("dataDescriptorURL"),
          PrimitiveToObjectWithValueField[String]("dataDescriptorURL")
        )
      )
    ),
    "doi" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("doi")),
    "license_info" -> FirstElement(
      ObjectArrayToListOfObject(
        "license",
        WriteObject(
          List(
            PrimitiveToObjectWithUrlField("url"),
            PrimitiveToObjectWithValueField[String]("name")
          )
        )
      )
    ),
    "component" -> ObjectArrayToListOfObject(
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
    "speciesFilter" -> PrimitiveArrayToListOfValueObject[String]("speciesFilter"),
    "embargoForFilter" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("embargoForFilter")),
    "external_datalink" -> Merge(
      PrimitiveArrayToListOfValueObject[String]("external_datalink"),
      PrimitiveArrayToListOfValueObject[String]("external_datalink"), // WORKAROUND
      {
        case (
          Some(ListOfObjectWithValueField(links)),
          _
          ) =>
          Some(
            ListOfObject(
              links.foldLeft(List[ObjectMap]()) {
                case (acc, currentValue) =>
                  (currentValue.toJson \ "value").asOpt[String] match {
                    case Some(v) =>
                      acc :+ ObjectMap(
                        List(
                          ObjectWithUrlField(Some(v)),
                          ObjectWithValueField[String](Some(v))
                        )
                      )
                    case _ => acc
                  }
              }
            )
          )
        case _ => None
      }
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
            case _ => None
          }
        ),
        {
          case (
            Some(ObjectWithValueField(Some(citationStr: String))),
            Some(ObjectWithValueField(Some(doiStr: String)))
            ) =>
            Some(ObjectWithValueField[String](Some(citationStr + "\n" + doiStr)))
          case (Some(ObjectWithValueField(Some(citationStr: String))), _) => Some(ObjectWithValueField[String](Some(citationStr.trim().stripSuffix(","))))
          case (_, Some(ObjectWithValueField(Some(doiStr: String)))) => Some(ObjectWithValueField[String](Some(doiStr)))
          case _ => None
        }
      )
    ),
    "atlas" -> PrimitiveArrayToListOfValueObject[String]("parcellationAtlas"),
    "region" -> ObjectArrayToListOfObject(
      "parcellationRegion",
      WriteObject(
        List(
          PrimitiveToObjectWithUrlField("url"),
          OrElse(PrimitiveToObjectWithValueField[String]("alias"), PrimitiveToObjectWithValueField[String]("name"))
        )
      )
    ),
    "preparation" -> PrimitiveArrayToListOfValueObject[String]("preparation"),
    "modalityForFilter" -> PrimitiveArrayToListOfValueObject[String]("modalityForFilter"),
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
        Merge(
          PrimitiveToObjectWithValueField[String](
            "title", {
              case Some(ObjectWithValueField(Some(str))) => Some(ObjectWithValueField(Some(str)))
              case _ => None
            }
          ),
          ObjectArrayToListOfObject(
            "neuroglancer",
            WriteObject(
              List(
                PrimitiveToObjectWithUrlField("url"),
                PrimitiveToObjectWithValueField[String](
                  "name", {
                    case Some(ObjectWithValueField(Some(str))) => Some(ObjectWithValueField(Some(str)))
                    case _ => None
                  }
                )
              )
            )
          ),
          {
            case (
              Some(ObjectWithValueField(titleStr)),
              Some(ListOfObject(obj: List[Object]))
              ) =>
              Some(
                ListOfObject(
                  obj.map(i => {
                    val url: Option[String] = (i.toJson \ "url").asOpt[String] match {
                      case Some(u) => Some(s"https://neuroglancer.humanbrainproject.org/?$u")
                      case _ => None
                    }
                    if ((i.toJson \ "value").isDefined) {
                      ObjectMap(
                        List(
                          ObjectWithUrlField(url),
                          ObjectWithValueField[String](Some("Show " + (i.toJson \ "value").as[String] + " in brain atlas viewer"))
                        )
                      )
                    } else {
                      titleStr match {
                        case Some(t) => ObjectMap(
                          List(
                            ObjectWithUrlField(url),
                            ObjectWithValueField[String](Some("Show " + t + " in brain atlas viewer"))
                          )
                        )
                        case _ => ObjectMap(
                          List(
                            ObjectWithUrlField(url),
                            ObjectWithValueField[String](Some("Show in brain atlas viewer"))
                          )
                        )
                      }
                    }
                  }
                  )
                ))
            case _ => None
          }
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
              Nested("species", PrimitiveArrayToListOfValueObject[String]("species")),
              Nested("sex", PrimitiveArrayToListOfValueObject[String]("sex")),
              Nested("age", PrimitiveToObjectWithValueField[String]("age")),
              Nested("agecategory", PrimitiveArrayToListOfValueObject[String]("agecategory")),
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
    "last_release" -> PrimitiveToObjectWithValueField[String]("last_release")
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
            case (
              _,
              Some(ObjectWithValueField(Some("Embargoed")))
              ) =>
              Some(
                ObjectWithValueField[String](
                  Some(
                    "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
                  )
                )
              )
            case (
              _,
              Some(ObjectWithValueField(Some("Under review")))
              ) =>
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
                  FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                    case (
                      Some(ObjectWithValueField(Some(preview: String))),
                      Some(ObjectWithValueField(Some(isAnimated: Boolean)))
                      ) =>
                      if (!preview.isBlank) {
                        Some(
                          NestedObject(
                            "previewUrl",
                            ObjectMap(
                              List(
                                ObjectWithUrlField(Some(preview)),
                                ObjectWithCustomField("isAnimated", Some(isAnimated))
                              )
                            )
                          )
                        )
                      } else {
                        None
                      }
                    case _ => None
                  }
                )
              ),
              Optional(
                Merge(
                  FirstElement(PrimitiveArrayToListOfValueObject[String]("static_image_url")),
                  FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                    case (
                      Some(ObjectWithValueField(Some(static: String))),
                      _
                      ) =>
                      if (!static.isBlank) {
                        Some(
                          NestedObject(
                            "staticImageUrl",
                            ObjectMap(
                              List(
                                ObjectWithUrlField(Some(static)),
                                ObjectWithCustomField("isAnimated", Some(false))
                              )
                            )
                          )
                        )
                      } else {
                        None
                      }
                    case _ => None
                  }
                )
              ),
              Optional(
                Merge(
                  FirstElement(PrimitiveArrayToListOfValueObject[String]("thumbnail_url")),
                  FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                    case (
                      Some(ObjectWithValueField(Some(thumbnail: String))),
                      _
                      ) =>
                      if (!thumbnail.isBlank) {
                        Some(
                          NestedObject(
                            "thumbnailUrl",
                            ObjectMap(
                              List(
                                ObjectWithUrlField(Some(thumbnail)),
                                ObjectWithCustomField("isAnimated", Some(false))
                              )
                            )
                          )
                        )
                      } else {
                        None
                      }
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
                    FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                      case (
                        Some(ObjectWithValueField(Some(preview: String))),
                        Some(ObjectWithValueField(Some(isAnimated: Boolean)))
                        ) =>
                        if (!preview.isBlank) {
                          Some(
                            NestedObject(
                              "previewUrl",
                              ObjectMap(
                                List(
                                  ObjectWithUrlField(Some(preview)),
                                  ObjectWithCustomField("isAnimated", Some(isAnimated))
                                )
                              )
                            )
                          )
                        } else {
                          None
                        }
                      case _ => None
                    }
                  )
                ),
                Optional(
                  Merge(
                    FirstElement(PrimitiveArrayToListOfValueObject[String]("static_image_url")),
                    FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                      case (
                        Some(ObjectWithValueField(Some(static: String))),
                        _
                        ) =>
                        if (!static.isBlank) {
                          Some(
                            NestedObject(
                              "staticImageUrl",
                              ObjectMap(
                                List(
                                  ObjectWithUrlField(Some(static)),
                                  ObjectWithCustomField("isAnimated", Some(false))
                                )
                              )
                            )
                          )
                        } else {
                          None
                        }
                      case _ => None
                    }
                  )
                ),
                Optional(
                  Merge(
                    FirstElement(PrimitiveArrayToListOfValueObject[String]("thumbnail_url")),
                    FirstElement(PrimitiveArrayToListOfValueObject[Boolean]("is_preview_animated")), {
                      case (
                        Some(ObjectWithValueField(Some(thumbnail: String))),
                        _
                        ) =>
                        if (!thumbnail.isBlank) {
                          Some(
                            NestedObject(
                              "thumbnailUrl",
                              ObjectMap(
                                List(
                                  ObjectWithUrlField(Some(thumbnail)),
                                  ObjectWithCustomField("isAnimated", Some(false))
                                )
                              )
                            )
                          )
                        } else {
                          None
                        }
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
