/*
 *   Copyright (c) 2018, EPFL/Human Brain Project PCO
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
package services.indexer

import java.net.URLEncoder

import models.DatabaseScope
import play.api.libs.json._
import utils.{Value, _}

sealed trait TemplateEngine[Content, TransformedContent] {
  def transform(c: Content, template: Map[String, Template]): TransformedContent
}

object TemplateEngine extends TemplateEngine[JsValue, JsValue] {
  override def transform(c: JsValue, template: Map[String, Template]): JsValue = {
    val currentContent = c.as[JsObject].value
    val transformedContent = template.foldLeft(Map[String, JsValue]()) {
      case (acc, (k, v)) =>
        v match {
          case opt @ Optional(_) =>
            opt.getAsOpt(currentContent) match {
              case Some(js) => acc.updated(k, js)
              case None     => acc
            }
          case _ =>
            acc.updated(k, v.op(currentContent))
        }
    }
    val j = Json.toJson(transformedContent)
    j
  }
}

trait DatasetTemplate {
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val template = Map(
    "identifier" -> Value("identifier"),
    "title"      -> Value("title"),
    "contributors" -> ObjectList(
      "contributors",
      ObjectValue(
        List(
          Reference(
            "relativeUrl",
            TemplateHelper.schemaIdToSearchId("Contributor")
          ),
          Value("name")
        ): _*
      )
    ),
    "citation" -> Merge(
      FirstElement(ValueList("citation")),
      FirstElement(ValueList("doi", doi => {
        val doiStr = doi.as[String]
        val url = URLEncoder.encode(doiStr, "UTF-8")
        JsString(s" [DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url")
      })),
      l =>
        r => {
          val strOpt = for {
            citationObj <- l.asOpt[JsObject]
            doiObj      <- r.asOpt[JsObject]
            citationJs  <- citationObj.value.get("value")
            doiJs       <- doiObj.value.get("value")
            citation    <- citationJs.asOpt[String]
            doi         <- doiJs.asOpt[String]
          } yield citation + doi
          Json.toJson(Map("value" -> strOpt.map(s => JsString(s)).getOrElse(JsNull)))
      }
    ),
    "zip"            -> Value("zip"),
    "dataDescriptor" -> Value("zip"),
    "doi"            -> FirstElement(ValueList("doi")),
    "license_info"   -> FirstElement(ObjectList("license", ObjectValue(Url("url"), Value("name")))),
    "component" -> FirstElement(
      ObjectList(
        "component",
        ObjectValue(List(Reference("relativeUrl", TemplateHelper.schemaIdToSearchId("Project")), Value("name")): _*)
      )
    ),
    "owners" -> FirstElement(
      ObjectList(
        "owners",
        ObjectValue(List(Reference("relativeUrl", TemplateHelper.schemaIdToSearchId("Contributor")), Value("name")): _*)
      )
    ),
    "description"      -> Value("description"),
    "speciesFilter"    -> FirstElement(ValueList("speciesFilter")),
    "embargoForFilter" -> FirstElement(ValueList("embargoForFilter")),
    "embargo" -> FirstElement(
      ValueList(
        "embargo",
        embargo => {
          embargo.asOpt[String].fold[JsValue](JsNull) {
            case "Embargoed" =>
              JsString(
                "This dataset is temporarily under embargo. The data will become available for download after the embargo period."
              )
            case "Under review" =>
              JsString(
                "This dataset is currently reviewed by the Data Protection Office regarding GDPR compliance. The data will be available after this review."
              )
          }
        }
      )
    ),
    "files" -> Optional(
      Merge(
        FirstElement(ValueList("embargo")),
        ObjectList(
          "files",
          ObjectValue(
            List(
              Merge(
                Url("absolute_path"),
                Value("private_access"),
                urlJs =>
                  privateAccessJs => {
                    val opt = for {
                      privateAccessObj  <- privateAccessJs.asOpt[JsObject]
                      privateAccessVal  <- privateAccessObj.value.get("value")
                      privateAccessBool <- privateAccessVal.asOpt[Boolean]
                      if privateAccessBool
                      urlObj <- urlJs.asOpt[JsObject]
                      urlVal <- urlObj.value.get("url")
                      urlStr <- urlVal.asOpt[String]
                    } yield Json.toJson(Map("url" -> s"$fileProxy/files/cscs?url=$urlStr"))
                    opt.getOrElse(urlJs)
                }
              ),
              Merge(
                Value("name"),
                Value("private_access"),
                name =>
                  privateAccess => {
                    val opt = for {
                      privateAccessObj  <- privateAccess.asOpt[JsObject]
                      privateAccessVal  <- privateAccessObj.value.get("value")
                      privateAccessBool <- privateAccessVal.asOpt[Boolean]
                      if privateAccessBool
                      nameOpt <- name.asOpt[JsObject]
                      nameVal <- nameOpt.value.get("value")
                      nameStr <- nameVal.asOpt[String]
                    } yield Json.toJson(Map("value" -> (s"ACCESS PROTECTED: $nameStr")))
                    opt.getOrElse(name)
                }
              ),
              new CustomField {
                override def fieldName: String = "human_readable_size"
                override def customField: String = "fileSize"
                override def transform: JsValue => JsValue = identity
              },
              Optional(
                Merge(
                  FirstElement(ValueList("preview_url")),
                  FirstElement(ValueList("is_preview_animated")),
                  preview =>
                    isAnimatedJs => {
                      val res = for {
                        isAnimatedObj  <- isAnimatedJs.asOpt[JsObject]
                        isAnimatedVal  <- isAnimatedObj.value.get("value")
                        isAnimatedBool <- isAnimatedVal.asOpt[Boolean]
                        previewObj     <- preview.asOpt[JsObject]
                        previewVal     <- previewObj.value.get("value")
                      } yield Map("previewUrl" -> Json.obj("url" -> previewVal, "isAnimated" -> isAnimatedBool))
                      res.map(Json.toJson(_)).getOrElse(JsNull)
                  }
                )
              )
            ): _*
          )
        ),
        embargo =>
          files => {
            embargo.asOpt[String].fold[JsValue](files)(_ => JsNull)
        }
      )
    ),
    "external_datalink" -> ObjectValue(Url("external_datalink"), Value("external_datalink")),
    "publications"      -> Value("zip"),
    "atlas"             -> Value("atlas"),
    "region"            -> Value("zip"),
    "preparation"       -> ValueList("preparation"),
    "methods"           -> ValueList("methods"),
    "protocol"          -> ValueList("protocol"),
    "viewer" -> OrElse(
      ObjectList(
        "brainviewer",
        ObjectValue(Url("url"), Value("name", js => JsString("Show " + js + " in brain atlas viewer")))
      ),
      ObjectList(
        "neuroglancer",
        ObjectValue(Url("url"), Value("title", js => JsString("Show " + js + " in brain atlas viewer")))
      ),
    ),
    "subjects"      -> Value("zip"),
    "first_release" -> Value("first_release"),
    "last_release"  -> Value("last_release"),
  )

}
