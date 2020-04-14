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
import models.templates.entities.{
  ListOfObject,
  ListOfObjectWithValueField,
  ObjectMap,
  ObjectWithUrlField,
  ObjectWithValueField,
  TemplateEntity
}
import utils.{
  FirstElement,
  Merge,
  ObjectArrayToListOfObject,
  Optional,
  PrimitiveArrayToListOfValueObject,
  PrimitiveToObjectWithUrlField,
  PrimitiveToObjectWithValueField,
  TemplateComponent,
  WriteObject
}

import scala.collection.immutable.HashMap

trait SoftwareProjectTemplate extends Template {
  def fileProxy: String

  def dataBaseScope: DatabaseScope

  val result: Map[String, TemplateComponent] = HashMap(
    "identifier" -> PrimitiveToObjectWithValueField[String]("identifier"),
    "title"      -> PrimitiveToObjectWithValueField[String]("title"),
    "description" -> Optional(
      Merge(
        PrimitiveToObjectWithValueField[String]("description", identity),
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveToObjectWithValueField[String]("description", identity)
            )
          )
        ),
        (descriptionOpt, versionDescriptionOpt) =>
          (descriptionOpt, versionDescriptionOpt) match {
            case (
                Some(ObjectWithValueField(Some(descriptionResult: String))),
                Some(ListOfObject(versionDescriptionList))
                ) =>
              versionDescriptionList
                .sortBy {
                  case ObjectMap(
                      List(
                        ObjectWithValueField(Some(versionRes: String)),
                        ObjectWithValueField(_)
                      )
                      ) =>
                    versionRes
                }
                .reverse
                .headOption match {
                case Some(ObjectMap(List(_, ObjectWithValueField(Some(versionDescriptionRes: String))))) =>
                  Some(ObjectWithValueField[String](Some(descriptionResult + "\n\n" + versionDescriptionRes)))
                case _ => Some(ObjectWithValueField[String](Some(descriptionResult)))
              }
            case (Some(ObjectWithValueField(Some(descriptionRes: String))), _) =>
              Some(ObjectWithValueField[String](Some(descriptionRes)))
            case (_, Some(ListOfObject(versionDescriptionList))) =>
              versionDescriptionList
                .sortBy {
                  case ObjectMap(
                      List(
                        ObjectWithValueField(Some(versionRes: String)),
                        ObjectWithValueField(_)
                      )
                      ) =>
                    versionRes
                }
                .reverse
                .headOption match {
                case Some(ObjectMap(List(_, ObjectWithValueField(Some(versionDescriptionRes: String))))) =>
                  Some(ObjectWithValueField[String](Some(versionDescriptionRes)))
                case _ => None
              }
            case _ => None
        }
      )
    ),
    "license" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("license", identity),
            ),
          ),
          listLicenseOpt => transformList(listLicenseOpt)
        ),
        licenseOpt => transformResult(licenseOpt)
      )
    ),
    "version" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity)
            ),
          ), {
            case res @ Some(ListOfObject(List(ObjectMap(List(ObjectWithValueField(None)))))) => res
            case Some(ListOfObject(versionLicenseList)) =>
              Some(ListOfObject(versionLicenseList.sortBy {
                case ObjectMap(
                    List(
                      ObjectWithValueField(Some(versionRes: String))
                    )
                    ) =>
                  versionRes
              }.reverse))
            case _ => None
          }
        ), {
          case Some(
              ObjectMap(
                List(
                  ObjectWithValueField(None)
                )
              )
              ) =>
            None
          case Some(
              ObjectMap(
                List(
                  ObjectWithValueField(Some(versionRes: String)),
                )
              )
              ) =>
            Some(ObjectWithValueField[String](Some(versionRes)))
          case _ => None
        }
      )
    ),
    "appCategory" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("applicationCategory", identity),
            ),
          ),
          listApplicationCategoryOpt => transformList(listApplicationCategoryOpt)
        ),
        appCategoryOpt => transformResult(appCategoryOpt)
      )
    ),
    "operatingSystem" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("operatingSystem", identity),
            ),
          ),
          listOperatingSystemOpt => transformList(listOperatingSystemOpt)
        ),
        operatingSystemOpt => transformResult(operatingSystemOpt)
      )
    ),
    "homepage" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("homepage", identity),
            ),
          ),
          listHomepageOpt => transformList(listHomepageOpt)
        ),
        homepageOpt => transformResultWithUrl(homepageOpt)
      )
    ),
    "sourceCode" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("sourceCode", identity),
            ),
          ),
          listSourceCodeOpt => transformList(listSourceCodeOpt)
        ),
        sourceCodeOpt => transformResultWithUrl(sourceCodeOpt)
      )
    ),
    "documentation" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("documentation", identity),
            ),
          ),
          listDocumentationOpt => transformList(listDocumentationOpt)
        ),
        documentationOpt => transformResultWithUrl(documentationOpt)
      )
    ),
    "features" -> Optional(
      FirstElement(
        ObjectArrayToListOfObject(
          "versions",
          WriteObject(
            List(
              PrimitiveToObjectWithValueField[String]("version", identity),
              PrimitiveArrayToListOfValueObject[String]("features", identity),
            ),
          ),
          listFeaturesOpt => transformList(listFeaturesOpt)
        ),
        featuresOpt => transformResult(featuresOpt)
      )
    )
  )

  private def transformList(l: Option[ListOfObject]): Option[ListOfObject] = {
    l match {
      case res @ Some(ListOfObject(List(ObjectMap(List(ObjectWithValueField(None), _))))) => res
      case Some(ListOfObject(versionLicenseList)) =>
        Some(ListOfObject(versionLicenseList.sortBy {
          case ObjectMap(
              List(
                ObjectWithValueField(Some(versionRes: String)),
                ListOfObjectWithValueField(List(_))
              )
              ) =>
            versionRes
        }.reverse))
      case _ => None
    }
  }

  private def transformResult(t: Option[TemplateEntity]): Option[TemplateEntity] = {
    t match {
      case Some(
          ObjectMap(
            List(
              ObjectWithValueField(_),
              ListOfObjectWithValueField(Nil)
            )
          )
          ) =>
        None
      case Some(
          ObjectMap(
            List(
              ObjectWithValueField(_),
              l
            )
          )
          ) =>
        Some(l)
      case _ => None
    }
  }

  private def transformResultWithUrl(t: Option[TemplateEntity]): Option[TemplateEntity] = {
    t match {
      case Some(
          ObjectMap(
            List(
              ObjectWithValueField(_),
              ListOfObjectWithValueField(Nil)
            )
          )
          ) =>
        None
      case Some(
          ObjectMap(
            List(
              ObjectWithValueField(_),
              ListOfObjectWithValueField(l)
            )
          )
          ) =>
        Some(
          ListOfObject(
            l.map {
                case ObjectWithValueField(Some(el: String)) =>
                  Some(ObjectMap(List(ObjectWithUrlField(Some(el)), ObjectWithValueField[String](Some(el)))))
                case _ => None
              }
              .collect {
                case Some(v) => v
              }
          )
        )
      case _ => None
    }
  }

  val template: Map[String, TemplateComponent] = dataBaseScope match {
    case INFERRED => HashMap("editorId" -> PrimitiveToObjectWithValueField[String]("editorId")) ++ result
    case _        => result
  }
}
