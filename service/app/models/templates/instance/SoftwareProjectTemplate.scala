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

import models.{DatabaseScope, INFERRED}
import models.templates.Template
import models.templates.entities.{ObjectValueList, ObjectValueMap, UrlObject, ValueObject, ValueObjectList}
import utils.{FirstElement, Merge, ObjectListReader, ObjectValue, Optional, TemplateComponent, Url, Value, ValueList}

import scala.collection.immutable.HashMap

trait SoftwareProjectTemplate extends Template {
  def fileProxy: String

  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> Value[String]("identifier"),
    "title" -> Value[String]("title"),
    "description" -> Optional(
      Merge(
        Value[String]("description", identity),
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              Value[String]("description", identity),
              //            Value[String]("license"),
              //            Value[String]("applicationCategory"),
              //            Value[String]("operatingSystem"),
              //            ObjectListReader("homepage", ObjectValue(List(Url("url"), Value[String]("value")))),
              //            ObjectListReader("sourceCode", ObjectValue(List(Url("url"), Value[String]("value")))),
              //            ObjectListReader("documentation", ObjectValue(List(Url("url"), Value[String]("value")))),
              //            Value[String]("features")
            )
          )
        ),
        (descriptionOpt, versionDescriptionOpt) =>
          (descriptionOpt, versionDescriptionOpt) match {
            case (Some(ValueObject(Some(descriptionResult: String))), Some(ObjectValueList(versionDescriptionList))) =>
              versionDescriptionList.sortBy {
                case ObjectValueMap(
                List(
                ValueObject(Some(versionRes: String)),
                ValueObject(Some(descriptionRes: String))
                )
                ) => versionRes
              }.reverse.headOption match {
                case Some(ObjectValueMap(List(_, ValueObject(Some(versionDescriptionRes: String))))) => Some(ValueObject[String](Some(descriptionResult + "\n\n" + versionDescriptionRes)))
                case _ => Some(ValueObject[String](Some(descriptionResult)))
              }
            case (Some(ValueObject(Some(descriptionRes: String))), _) => Some(ValueObject[String](Some(descriptionRes)))
            case (_, Some(ObjectValueList(versionDescriptionList))) =>
              versionDescriptionList.sortBy {
                case ObjectValueMap(
                List(
                ValueObject(Some(versionRes: String)),
                ValueObject(Some(descriptionRes: String))
                )
                ) => versionRes
              }.reverse.headOption match {
                case Some(ObjectValueMap(List(_, ValueObject(Some(versionDescriptionRes: String))))) => Some(ValueObject[String](Some(versionDescriptionRes)))
                case _ => None
              }
            case _ => None
          }
      )
    ),
    "license" -> Optional(
      Merge(
        Value[String]("description", identity),
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("license", identity),
            )
          )
        ),
        (descriptionOpt, licenseOpt) =>
          (descriptionOpt,licenseOpt) match  {
            case (_, Some(ObjectValueList(versionLicenseList))) =>
              versionLicenseList.sortBy {
                case ObjectValueMap(
                List(
                ValueObject(Some(versionRes: String)),
                ValueObjectList(List(ValueObject(Some(licenseRes: String))))
                )
                ) => versionRes
              }.reverse.headOption match {
                case Some(ObjectValueMap(List(_, ValueObjectList(List(ValueObject(Some(versionLicenseRes: String))))))) => Some(ValueObjectList(List(ValueObject(Some(versionLicenseRes)))))
                case _ => None
              }
            case _ => None
          }
      )
    )
  )

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> Value[String]("editorId")) ++ result
    case _ => result
  }
}
