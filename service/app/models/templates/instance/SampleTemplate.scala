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

import models.templates.Template
import models.templates.entities.{ObjectWithValueField, _}
import models.{DatabaseScope, INFERRED}
import play.api.libs.json.{JsNull, JsString}
import utils._

import scala.collection.immutable.HashMap

trait SampleTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier"        -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title"             -> PrimitiveToObjectWithValueField[String]("title"),
    "weightPreFixation" -> PrimitiveToObjectWithValueField[String]("weightPreFixation"),
    "parcellationAtlas" -> PrimitiveArrayToListOfValueObject[String]("parcellationAtlas"),
    "region" ->
    ObjectArrayToListOfObject(
      "parcellationRegion",
      WriteObject(
        List(
          Optional(PrimitiveToObjectWithUrlField("url")),
          OrElse(PrimitiveToObjectWithValueField[String]("alias"), PrimitiveToObjectWithValueField[String]("name"))
        )
      )
    ),
    "viewer" -> ObjectArrayToListOfObject(
      "brainviewer",
      WriteObject(
        List(
          PrimitiveToObjectWithUrlField("url"),
          PrimitiveToObjectWithValueField[String]("name", js => js.map(str => "Show " + str + " in brain atlas viewer"))
        )
      )
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
                SetValue("detail", JsString("###HBP Knowledge Graph Data Platform Citation Requirements")),
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
              FirstElement(
                PrimitiveArrayToListOfValueObject[String](
                  "componentName",
                )
              )
            ),
            Nested(
              "name",
              FirstElement(
                ObjectArrayToListOfObject(
                  "instances",
                  WriteObject(
                    List(
                      PrimitiveToObjectWithReferenceField(
                        "identifier",
                        ref => ref.map(TemplateHelper.refUUIDToSearchId("Dataset"))
                      ),
                      PrimitiveToObjectWithValueField[String]("name")
                    )
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
    "first_release" -> PrimitiveToObjectWithValueField[String]("first_release"),
    "last_release"  -> PrimitiveToObjectWithValueField[String]("last_release"),
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _        => result
  }
}
