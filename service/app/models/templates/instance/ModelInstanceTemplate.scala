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

import models.templates.{FileProxy, Template}
import models.templates.entities.{ListOfObject, NestedObject, ObjectMap, ObjectWithUrlField, ObjectWithValueField, SetValue, ObjectWithCustomField}
import models.{INFERRED, RELEASED}
import play.api.libs.json.JsString
import utils.{PrimitiveToObjectWithValueField, _}

import scala.collection.immutable.HashMap

trait ModelInstanceTemplate extends Template with FileProxy {

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title" -> PrimitiveToObjectWithValueField[String]("title"),
    "description" -> PrimitiveToObjectWithValueField[String]("description"),
    "version" -> PrimitiveToObjectWithValueField[String]("version"),
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
    "contributors" -> ObjectArrayToListOfObject(
      "contributors",
      WriteObject(
        List(
          if(liveMode){
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(s => s"$s")
            )
          } else{
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Contributor/$s")
            )
          },
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "owners" -> ObjectArrayToListOfObject(
      "custodian",
      WriteObject(
        List(
          if(liveMode){
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(s => s"$s")
            )
          } else {
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Contributor/$s")
            )
          },
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "mainContact" -> ObjectArrayToListOfObject(
      "mainContact",
      WriteObject(
        List(
          if(liveMode){
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(s => s"$s")
            )
          } else {
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Contributor/$s")
            )
          },
          PrimitiveToObjectWithValueField[String]("name")
        )
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
            case s => s
          }
        ),
        (citation, doi) => {
          (citation, doi) match {
            case (Some(ObjectWithValueField(Some(citationStr))), Some(ObjectWithValueField(Some(doiStr)))) =>
              Some(ObjectWithValueField[String](Some(citationStr + "\n" + doiStr)))
            case _ => doi
          }
        }
      )
    ),
    "allfiles" -> Optional(
      Merge(
        FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo")),
        ObjectArrayToListOfObject(
          "fileBundle",
          WriteObject(
            List(
              PrimitiveToObjectWithUrlField("url"),
              PrimitiveToObjectWithValueField[String]("value")
            )
          )
        ),
        (embargoOpt, urlOpt) =>
          (embargoOpt, urlOpt) match {
            case (Some(ObjectWithValueField(Some("embargoed"))), _) => None
            case (_, Some(ListOfObject(listRes))) =>
              Some(ListOfObject(listRes.map {
                case ObjectMap(
                List(
                ObjectWithUrlField(Some(resUrl)),
                _
                )
                ) if resUrl.startsWith("https://object.cscs.ch") =>
                  ObjectMap(
                    List(
                      ObjectWithUrlField(Some(s"https://kg.ebrains.eu/proxy/export?container=$resUrl")),
                      ObjectWithValueField[String](Some("download all related data as ZIP"))
                    )
                  )
                case ObjectMap(List(ObjectWithUrlField(Some(resUrl)), _)) =>
                  ObjectMap(
                    List(
                      ObjectWithUrlField(Some(resUrl)),
                      ObjectWithValueField[String](Some("Go to the data"))
                    )
                  )
                case s => s
              }))
            case _ => None
          }
      )
    ),
    "brainStructures" -> PrimitiveArrayToListOfValueObject[String]("brainStructure"),
    "cellularTarget" -> PrimitiveArrayToListOfValueObject[String]("cellularTarget"),
    "studyTarget" -> PrimitiveArrayToListOfValueObject[String]("studyTarget"),
    "modelScope" -> PrimitiveArrayToListOfValueObject[String]("modelScope"),
    "abstractionLevel" -> PrimitiveArrayToListOfValueObject[String]("abstractionLevel"),
    "modelFormat" -> PrimitiveArrayToListOfValueObject[String]("modelFormat"),
    "usedDataset" -> ObjectArrayToListOfObject(
      "usedDataset",
      WriteObject(
        List(
          if(liveMode){
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(s => s"$s")
            )
          } else {
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Dataset/$s")
            )
          },
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "producedDataset" -> ObjectArrayToListOfObject(
      "producedDataset",
      WriteObject(
        List(
          if(liveMode){
            PrimitiveToObjectWithReferenceField(
              "relativeUrl",
              ref => ref.map(s => s"$s")
            )
          } else {
            PrimitiveToObjectWithReferenceField(
              "identifier",
              ref => ref.map(s => s"Dataset/$s")
            )
          },
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "first_release" -> PrimitiveToObjectWithValueField[String]("firstReleaseAt"),
    "last_release" -> PrimitiveToObjectWithValueField[String]("lastReleaseAt")
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap(
      "editorId" -> PrimitiveToObjectWithValueField[String]("editorId"),
      "embargo" -> Merge(
        ObjectArrayToListOfObject(
          "fileBundle",
          WriteObject(
            List(
              PrimitiveToObjectWithUrlField("url")
            )
          )
        ),
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
              Some(ListOfObject(objList: List[Object])),
              Some(ObjectWithValueField(Some("embargoed")))
              ) => {
              val url = if (objList.isEmpty) {
                None
              } else {
                (objList.head.toJson \ "url").asOpt[String] match {
                  case Some(u) => Some(u)
                  case _ => None
                }
              }
              url match {
                case Some(urlRes) => if (urlRes.startsWith("https://object.cscs.ch")) {
                  Some(
                    ObjectWithValueField[String](
                      Some {
                        "This model is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=" + urlRes + "\" target=\"_blank\"> you should be able to access the data here</a>"
                      }
                    )
                  )
                }
                else {
                  Some(
                    ObjectWithValueField[String](
                      Some(
                        "This model is temporarily under embargo. The data will become available for download after the embargo period."
                      )
                    )
                  )
                }
                case _ => Some(
                    ObjectWithValueField[String](
                      Some(
                        "This model is temporarily under embargo. The data will become available for download after the embargo period."
                      )
                    )
                  )
              }
            }
            case _ => None
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
            "embargo", {
              case ObjectWithValueField(Some("embargoed")) =>
                ObjectWithValueField[String](
                  Some(
                    "This model is temporarily under embargo. The data will become available for download after the embargo period."
                  )
                )
              case _ => ObjectWithValueField[String](None)
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
          ), {
            case (
              Some(ObjectWithValueField(Some(embargoString: String))),
              filesOpt
              ) if (embargoString != "embargoed") =>
              filesOpt
            case (None, filesOpt) => filesOpt
            case _ => None
          }
        )
      )
    ) ++ result
  }

}
