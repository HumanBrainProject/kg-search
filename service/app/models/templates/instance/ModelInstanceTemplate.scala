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
import models.templates.entities.{ObjectValueList, ObjectValueMap, SetValue, UrlObject, ValueObject}
import models.{DatabaseScope, INFERRED}
import play.api.libs.json.{JsNull, JsString, JsValue}
import utils.{Value, _}

import scala.collection.immutable.HashMap

trait ModelInstanceTemplate extends Template {
  def fileProxy: String

  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier"  -> Value[String]("identifier", identity),
    "title"       -> Value[String]("title", identity),
    "description" -> Value[String]("description", identity),
    "version"     -> Value[String]("version", identity),
    "contributors" -> ObjectListReader(
      "contributors",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          Value[String]("name", identity)
        )
      )
    ),
    "owners" -> ObjectListReader(
      "custodian",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          Value[String]("name", identity)
        )
      )
    ),
    "mainContact" -> ObjectListReader(
      "mainContact",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Contributor"))),
          Value[String]("name", identity)
        )
      )
    ),
    "publications" -> ObjectListReader(
      "publications",
      Merge(
        Value[String]("citation", identity),
        Value[String]("doi", doi => {
          doi.map { doiStr =>
            val url = URLEncoder.encode(doiStr, "UTF-8")
            s"[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url"
          }
        }),
        (citation, doi) => {
          (citation, doi) match {
            case (Some(ValueObject(Some(citationStr))), Some(ValueObject(Some(doiStr)))) =>
              Some(ValueObject[String](Some(citationStr + "\n" + doiStr)))
            case _ => doi
          }
        }
      )
    ),
    "embargo" -> Optional(
      FirstElement(
        ValueList[String](
          "embargo", {
            case ValueObject(Some("embargoed")) =>
              ValueObject[String](
                Some(
                  "This model is temporarily under embargo. The data will become available for download after the embargo period."
                )
              )
            case _ => ValueObject[String](None)
          }
        )
      )
    ),
    "allFiles" -> Optional(
      Merge(
        FirstElement(ValueList[String]("embargo", identity)),
        ObjectListReader(
          "fileBundle",
          ObjectValue(
            List(
              Url("url"),
              Set("detail", JsNull),
              Value[String]("value", identity)
            )
          )
        ),
        (embargoOpt, urlOpt) =>
          (embargoOpt, urlOpt) match {
            case (Some(ValueObject(Some("embargoed"))), _) => None
            case (_, Some(ObjectValueList(listRes))) =>
              Some(ObjectValueList(listRes.map {
                case ObjectValueMap(
                    List(
                      UrlObject(Some(resUrl)),
                      SetValue(_, _),
                      ValueObject(_)
                    )
                    ) if resUrl.startsWith("https://object.cscs.ch") =>
                  ObjectValueMap(
                    List(
                      UrlObject(Some(s"https://kg.ebrains.eu/proxy/export?container=$resUrl")),
                      SetValue("detail", JsString("###HBP Knowledge Graph Data Platform Citation Requirements")),
                      ValueObject[String](Some("download all related data as ZIP"))
                    )
                  )
                case ObjectValueMap(List(UrlObject(Some(resUrl)), x)) =>
                  ObjectValueMap(
                    List(
                      UrlObject(Some(resUrl)),
                      ValueObject[String](Some("Go to the data"))
                    )
                  )
                case s => s
              }))
            case _ => None
        }
      )
    ),
    "brainStructures"  -> ValueList[String]("brainStructure", identity),
    "cellularTarget"   -> ValueList[String]("cellularTarget", identity),
    "studyTarget"      -> ValueList[String]("studyTarget", identity),
    "modelScope"       -> ValueList[String]("modelScope", identity),
    "abstractionLevel" -> ValueList[String]("abstractionLevel", identity),
    "modelFormat"      -> ValueList[String]("modelFormat", identity),
    "first_release"    -> Value[String]("first_release", identity),
    "last_release"     -> Value[String]("last_release", identity),
    "usedDataset" -> ObjectListReader(
      "usedDataset",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Dataset"))),
          Value[String]("name", identity)
        )
      )
    ),
    "producedDataset" -> ObjectListReader(
      "producedDataset",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Dataset"))),
          Value[String]("name", identity)
        )
      )
    )
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> Value[String]("editorId", identity)) ++ result
    case _        => result
  }

}