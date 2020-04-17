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
import models.templates.entities.ObjectWithValueField
import utils.{
  Merge,
  ObjectArrayToListOfObject,
  PrimitiveToObjectWithReferenceField,
  PrimitiveToObjectWithValueField,
  TemplateComponent,
  TemplateHelper,
  WriteObject
}

import scala.collection.immutable.HashMap

trait UnimindsPersonTemplate extends Template {

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier"  -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title"       -> PrimitiveToObjectWithValueField[String]("title"),
    "description" -> PrimitiveToObjectWithValueField[String]("description"),
    "phone"       -> PrimitiveToObjectWithValueField[String]("phone"),
    "custodianOf" -> ObjectArrayToListOfObject(
      "custodianOf",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "identifier",
            ref => ref.map(s => s"Dataset/$s")
          ),
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "custodianOfModel" -> ObjectArrayToListOfObject(
      "custodianOfModel",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "identifier",
            ref => ref.map(s => s"Model/$s")
          ),
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "address" -> PrimitiveToObjectWithValueField[String]("address"),
    "contributions" -> ObjectArrayToListOfObject(
      "contributions",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "identifier",
            ref => ref.map(s => s"Dataset/$s")
          ),
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
    "modelContributions" -> ObjectArrayToListOfObject(
      "modelContributions",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField(
            "identifier",
            ref => ref.map(s => s"Model/$s")
          ),
          PrimitiveToObjectWithValueField[String]("name")
        )
      )
    ),
    "email"         -> PrimitiveToObjectWithValueField[String]("email"),
    "first_release" -> PrimitiveToObjectWithValueField[String]("first_release"),
    "last_release"  -> PrimitiveToObjectWithValueField[String]("last_release")
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _        => result
  }
}
