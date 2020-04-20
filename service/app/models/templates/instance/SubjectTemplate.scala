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
import utils._

import scala.collection.immutable.HashMap

trait SubjectTemplate extends Template {

  val result: Map[String, TemplateComponent] = Map(
    "identifier"  -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title"       -> PrimitiveToObjectWithValueField[String]("title"),
    "species"     -> FirstElement(PrimitiveArrayToListOfValueObject[String]("species")),
    "sex"         -> FirstElement(PrimitiveArrayToListOfValueObject[String]("sex")),
    "age"         -> PrimitiveToObjectWithValueField[String]("age"),
    "agecategory" -> FirstElement(PrimitiveArrayToListOfValueObject[String]("agecategory")),
    "weight"      -> PrimitiveToObjectWithValueField[String]("weight"),
    "strain" -> OrElse(
      PrimitiveToObjectWithValueField[String]("strain"),
      PrimitiveToObjectWithValueField[String]("strains")
    ),
    "genotype" -> PrimitiveToObjectWithValueField[String]("genotype"),
    "samples" -> ObjectArrayToListOfObject(
      "samples",
      WriteObject(
        List(
          PrimitiveToObjectWithReferenceField("identifier", ref => ref.map(s => s"Sample/$s")),
          PrimitiveToObjectWithValueField[String]("name"),
        )
      )
    ),
    "datasetExists" -> PrimitiveArrayToListOfValueObject[String]("datasetExists"),
    "datasets" -> ObjectArrayToListOfObject(
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
              ObjectArrayToListOfObject(
                "instances",
                WriteObject(
                  List(
                    PrimitiveToObjectWithReferenceField(
                      "identifier",
                      ref => ref.map(s => s"Dataset/$s")
                    ),
                    PrimitiveToObjectWithValueField[String]("name")
                  )
                )
              )
            )
          )
        )
      )
    ),
    "first_release" -> PrimitiveToObjectWithValueField[String]("first_release"),
    "last_release"  -> PrimitiveToObjectWithValueField[String]("last_release"),
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => Map("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _        => result
  }
}
