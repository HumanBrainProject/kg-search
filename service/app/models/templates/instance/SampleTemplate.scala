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
import models.templates.entities.{ValueObject, _}
import models.{DatabaseScope, INFERRED}
import play.api.libs.json.{JsNull, JsString}
import utils._

import scala.collection.immutable.HashMap

trait SampleTemplate extends Template {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier"        -> Value[String]("identifier"),
    "title"             -> Value[String]("title"),
    "weightPreFixation" -> Value[String]("weightPreFixation"),
    "parcellationAtlas" -> ValueList[String]("parcellationAtlas"),
    "region" ->
    ObjectListReader(
      "parcellationRegion",
      ObjectValue(List(Optional(Url("url")), OrElse(Value[String]("alias"), Value[String]("name"))))
    ),
    "viewer" -> ObjectListReader(
      "brainviewer",
      ObjectValue(
        List(Url("url"), Value[String]("name", js => js.map(str => "Show " + str + " in brain atlas viewer")))
      )
    ),
    "methods" -> ValueList[String]("methods"),
    "allfiles" ->
    Merge(
      FirstElement(ValueList[String]("embargo", identity)),
      Value[String]("container_url"), {
        case (Some(ValueObject(Some("Embargoed"))), _) => None
        case (_, Some(ValueObject(Some(containerUrl: String)))) if containerUrl.startsWith("https://object.cscs.ch") =>
          Some(
            ObjectValueMap(
              List(
                UrlObject(Some(s"https://kg.ebrains.eu/proxy/export?container=$containerUrl")),
                SetValue("detail", JsString("###HBP Knowledge Graph Data Platform Citation Requirements")),
                ValueObject[String](Some("download all related data as ZIP"))
              )
            )
          )
        case (_, Some(ValueObject(Some(containerUrl: String)))) =>
          Some(
            ObjectValueMap(
              List(
                UrlObject(Some(containerUrl)),
                ValueObject[String](Some("Go to the data"))
              )
            )
          )
        case _ => None
      }
    ),
    "files" ->
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
    ),
    "datasets" ->
    ObjectListReader(
      "datasets",
      Nested(
        "children",
        ObjectValue(
          List(
            Nested(
              "component",
              FirstElement(
                ValueList[String](
                  "componentName",
                )
              )
            ),
            Nested(
              "name",
              FirstElement(
                ObjectListReader(
                  "instances",
                  ObjectValue(
                    List(
                      Reference(
                        "identifier",
                        ref => ref.map(TemplateHelper.refUUIDToSearchId("Dataset"))
                      ),
                      Value[String]("name")
                    )
                  )
                )
              )
            )
          )
        )
      )
    ),
    "datasetExists" -> ValueList[String]("datasetExists"),
    "subject" ->
    ObjectListReader(
      "subjects",
      Nested(
        "children",
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
            Nested("strain", OrElse(Value[String]("strain"), Value[String]("strains"))),
            Nested("genotype", Value[String]("genotype"))
          )
        )
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
