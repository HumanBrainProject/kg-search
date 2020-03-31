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
import models.templates.entities.ValueObject
import utils.{Merge, ObjectListReader, ObjectValue, Reference, TemplateComponent, TemplateHelper, Value}

import scala.collection.immutable.HashMap

trait ProjectTemplate extends Template {
  def fileProxy: String

  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> Value[String]("identifier", identity),
    "title" -> Value[String]("title", identity),
    "description" -> Value[String]("description", identity),
    "dataset" -> ObjectListReader(
      "datasets",
      ObjectValue(
        List(
          Reference("relativeUrl", ref => ref.map(TemplateHelper.schemaIdToSearchId("Dataset"))),
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
        citation =>
          doi => {
            (citation, doi) match {
              case (Some(citationObj: ValueObject[String]), Some(doiObj: ValueObject[String])) =>
                val strOpt = for {
                  citationStr <- citationObj.value
                  doiStr <- doiObj.value
                } yield citationStr + "\n" + doiStr
                strOpt.map(str => ValueObject[String](Some(str)))
              case _ => doi
            }

          }
      )
    ),
    "first_release" -> Value[String]("first_release", identity),
    "last_release" -> Value[String]("last_release", identity)
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> Value[String]("editorId", identity)) ++ result
    case _ => result
  }
}
