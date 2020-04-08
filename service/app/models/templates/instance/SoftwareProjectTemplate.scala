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
import models.templates.entities.{ObjectValueList, ObjectValueMap, TemplateEntity, UrlObject, ValueObject, ValueObjectList}
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
              Value[String]("description", identity)
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
                ValueObject(_)
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
                ValueObject(_)
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
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("license", identity),
            ),
          ),
          listLicenseOpt => transformList(listLicenseOpt)
        ),
        licenseOpt => transformResult(licenseOpt)
      )
    ),
    "version" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity)
            ),
          ),
          {
            case res@Some(ObjectValueList(List(ObjectValueMap(List(ValueObject(None)))))) => res
            case Some(ObjectValueList(versionLicenseList)) =>
              Some(ObjectValueList(versionLicenseList.sortBy {
                case ObjectValueMap(
                List(
                ValueObject(Some(versionRes: String))
                )
                ) => versionRes
              }.reverse))
            case _ => None
          }
        ),
        {
          case Some(
            ObjectValueMap(
              List(
                ValueObject(None)
              )
            )
          ) => None
          case Some(
            ObjectValueMap(
              List(
                ValueObject(Some(versionRes: String)),
              )
            )
          ) => Some(ValueObject[String](Some(versionRes)))
          case _ => None
        }
      )
    ),
    "appCategory" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("applicationCategory", identity),
            ),
          ),
          listApplicationCategoryOpt => transformList(listApplicationCategoryOpt)
        ),
        appCategoryOpt => transformResult(appCategoryOpt)
      )
    ),
    "operatingSystem" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("operatingSystem", identity),
            ),
          ),
          listOperatingSystemOpt => transformList(listOperatingSystemOpt)
        ),
        operatingSystemOpt => transformResult(operatingSystemOpt)
      )
    ),
    "homepage" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("homepage", identity),
            ),
          ),
          listHomepageOpt => transformList(listHomepageOpt)
        ),
        homepageOpt => transformResultWithUrl(homepageOpt)
      )
    ),
    "sourceCode" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("sourceCode", identity),
            ),
          ),
          listSourceCodeOpt => transformList(listSourceCodeOpt)
        ),
        sourceCodeOpt => transformResultWithUrl(sourceCodeOpt)
      )
    ),
    "documentation" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("documentation", identity),
            ),
          ),
          listDocumentationOpt => transformList(listDocumentationOpt)
        ),
        documentationOpt => transformResultWithUrl(documentationOpt)
      )
    ),
    "features" -> Optional(
      FirstElement(
        ObjectListReader(
          "versions",
          ObjectValue(
            List(
              Value[String]("version", identity),
              ValueList[String]("features", identity),
            ),
          ),
          listFeaturesOpt => transformList(listFeaturesOpt)
        ),
        featuresOpt => transformResult(featuresOpt)
      )
    )
  )

  private def transformList(l: Option[ObjectValueList]):Option[ObjectValueList] = {
    l match {
      case res@Some(ObjectValueList(List(ObjectValueMap(List(ValueObject(None), _))))) => res
      case Some(ObjectValueList(versionLicenseList)) =>
        Some(ObjectValueList(versionLicenseList.sortBy {
          case ObjectValueMap(
          List(
          ValueObject(Some(versionRes: String)),
          ValueObjectList(List(_))
          )
          ) => versionRes
        }.reverse))
      case _ => None
    }
  }

  private def transformResult(t: Option[TemplateEntity]): Option[TemplateEntity] = {
    t match {
      case Some(
        ObjectValueMap(
          List(
            ValueObject(_),
            ValueObjectList(Nil)
          )
        )
      ) => None
      case Some(
        ObjectValueMap(
          List(
            ValueObject(_),
            l
          )
        )
      ) => Some(l)
      case _ => None
    }
  }

  private def transformResultWithUrl(t: Option[TemplateEntity]): Option[TemplateEntity] = {
    t match {
      case Some(
        ObjectValueMap(
          List(
            ValueObject(_),
            ValueObjectList(Nil)
          )
        )
      ) => None
      case Some(
        ObjectValueMap(
          List(
            ValueObject(_),
            ValueObjectList(l)
          )
        )
      ) =>  Some(
        ObjectValueList(
          l.map {
            case ValueObject(Some(el:String)) => Some(ObjectValueMap(List(UrlObject(Some(el)), ValueObject[String](Some(el)))))
            case _ => None
          }.collect{
            case Some(v) => v
          }
        )
      )
      case _ => None
    }
  }

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> Value[String]("editorId")) ++ result
    case _ => result
  }
}
