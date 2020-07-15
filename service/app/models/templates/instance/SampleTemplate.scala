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
import models.templates.entities.{ObjectWithValueField, _}
import models.{DatabaseScope, INFERRED}
import play.api.libs.json.{JsNull, JsString}
import utils._

import scala.collection.immutable.HashMap

trait SampleTemplate extends Template with FileProxy {

  val result: Map[String, TemplateComponent] = Map(
    "identifier" -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title" -> PrimitiveToObjectWithValueField[String]("title"),
    "weightPreFixation" -> PrimitiveToObjectWithValueField[String]("weightPreFixation"),
    "parcellationAtlas" -> PrimitiveArrayToListOfValueObject[String]("parcellationAtlas"),
    "region" ->
      ObjectArrayToListOfObject(
        "parcellationRegion",
        WriteObject(
          List(
            Optional(PrimitiveToObjectWithUrlField("url")),
            OrElse(
              PrimitiveToObjectWithValueField[String]("alias"),
              PrimitiveToObjectWithValueField[String]("name")
            )
          )
        )
      ),
    "viewer" ->
      Merge(
        PrimitiveToObjectWithValueField[String](
          "title", {
            case Some(ObjectWithValueField(Some(str))) => Some(ObjectWithValueField(Some(str)))
            case _ => None
          }
        ),
        PrimitiveArrayToListOfValueObject[String]("brainViewer"),
        {
          case (
            Some(ObjectWithValueField(titleStr)),
            Some(ListOfObjectWithValueField(url))
            ) =>
            Some(
              ListOfObject(
                url.foldLeft(List[ObjectMap]()) {
                  case (acc, urlRes) => urlRes match {
                    case ObjectWithValueField(Some(urlRes: String)) => acc :+ ObjectMap(
                      List(
                        ObjectWithUrlField(Some(urlRes)),
                        titleStr match {
                          case Some(t) => ObjectWithValueField[String](Some("Show " + t + " in brain atlas viewer"))
                          case _ => ObjectWithValueField[String](Some("Show in brain atlas viewer"))
                        }
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
    "methods" -> PrimitiveArrayToListOfValueObject[String]("methods"),
    "allfiles" ->
      Merge(
        FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo", identity)),
        PrimitiveToObjectWithValueField[String]("container_url"), {
          case (Some(ObjectWithValueField(Some("Embargoed"))), _) => None
          case (_, Some(ObjectWithValueField(Some(containerUrl: String))))
            if containerUrl.startsWith("https://object.cscs.ch") =>
            Some(
              ObjectMap(
                List(
                  ObjectWithUrlField(Some(s"https://kg.ebrains.eu/proxy/export?container=$containerUrl")),
                  ObjectWithValueField[String](Some("download all related data as ZIP"))
                )
              )
            )
          case (_, Some(ObjectWithValueField(Some(containerUrl: String)))) =>
            Some(
              ObjectMap(
                List(
                  ObjectWithUrlField(Some(containerUrl)),
                  ObjectWithValueField[String](Some("Go to the data"))
                )
              )
            )
          case _ => None
        }
      ),
    "files" ->
      Merge(
        FirstElement(PrimitiveArrayToListOfValueObject[String]("embargo")),
        ObjectArrayToListOfObject(
          "files",
          WriteObject(
            List(
              Merge(
                WriteObject(
                  List(PrimitiveToObjectWithUrlField("absolutePath"), PrimitiveToObjectWithValueField[String]("name"))
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
      ),
    "datasets" ->
      ObjectArrayToListOfObject(
        "datasets",
        Nested(
          "children",
          WriteObject(
            List(
              Nested(
                "component",
                PrimitiveArrayToListOfValueObject[String](
                  "componentName",
                )
              ),
              Nested(
                "name",
                ObjectArrayToListOfObject(
                  "instances",
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
                )
              )
            )
          )
        )
      ),
    "datasetExists" -> PrimitiveArrayToListOfValueObject[String]("datasetExists"),
    "subject" ->
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
                    if(liveMode){
                      PrimitiveToObjectWithReferenceField(
                        "relativeUrl",
                        ref => ref.map(s => s"$s")
                      )
                    } else {
                      PrimitiveToObjectWithReferenceField(
                        "identifier",
                        ref => ref.map(s => s"Subject/$s")
                      )
                    },
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
                OrElse(
                  PrimitiveToObjectWithValueField[String]("strain"),
                  PrimitiveToObjectWithValueField[String]("strains")
                )
              ),
              Nested("genotype", PrimitiveToObjectWithValueField[String]("genotype"))
            )
          )
        )
      ),
    "first_release" -> PrimitiveToObjectWithValueField[String]("firstReleaseAt"),
    "last_release"  -> PrimitiveToObjectWithValueField[String]("lastReleaseAt")
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => Map("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _ => result
  }
}
