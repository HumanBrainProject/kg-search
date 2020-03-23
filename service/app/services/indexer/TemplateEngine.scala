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
  type Id[A] = A
  def fileProxy: String
  def dataBaseScope: DatabaseScope

  val template = Map(
    "identifier" -> Value("identifier"),
    "title"      -> Value("title"),
    "contributors" -> ObjectList(
      "contributors",
      ObjectValue(
        Reference(
          "relativeUrl",
          Value("name"),
          TemplateHelper.schemaIdToSearchId("Contributor")
        )
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
    "license_info"   -> FirstElement(ObjectList("license", Url("url", Value("name")))),
    "component" -> FirstElement(
      ObjectList(
        "component",
        Reference("relativeUrl", Value("name"), TemplateHelper.schemaIdToSearchId("Project"))
      )
    ),
    "owners" -> FirstElement(
      ObjectList(
        "owners",
        Reference("relativeUrl", Value("name"), TemplateHelper.schemaIdToSearchId("Contributor"))
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
                Url("absolute_path", Value("name")),
                Value("private_access"),
                urlJs =>
                  privateAccessJs => {
                    val opt = for {
                      privateAccessObj  <- privateAccessJs.asOpt[JsObject]
                      privateAccessVal  <- privateAccessObj.value.get("value")
                      privateAccessBool <- privateAccessVal.asOpt[Boolean]
                      if privateAccessBool
                      urlObj  <- urlJs.asOpt[JsObject]
                      urlVal  <- urlObj.value.get("url")
                      urlStr  <- urlVal.asOpt[String]
                      nameVal <- urlObj.value.get("value")
                      nameStr <- nameVal.asOpt[String]
                    } yield
                      Json.toJson(
                        Map("url" -> s"$fileProxy/files/cscs?url=$urlStr", "value" -> (s"ACCESS PROTECTED: $nameStr"))
                      )
                    opt.getOrElse(urlJs)
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
    "external_datalink" ->
    Url("external_datalink", Value("external_datalink")),
    "publications" -> ObjectList(
      "publications",
      Merge(
        Value("citation"),
        Value("doi", doi => {
          val doiStr = doi.as[String]
          val url = URLEncoder.encode(doiStr, "UTF-8")
          JsString(s"\n[DOI: $doiStr]\n[DOI: $doiStr]: https://doi.org/$url")
        }),
        citation =>
          doi => {
            val strOpt = for {
              citationObj <- citation.asOpt[JsObject]
              doiObj      <- doi.asOpt[JsObject]
              citationJs  <- citationObj.value.get("value")
              doiJs       <- doiObj.value.get("value")
              citationStr <- citationJs.asOpt[String]
              doiStr      <- doiJs.asOpt[String]
            } yield citationStr + doiStr
            Json.toJson(Map("value" -> strOpt.map(s => JsString(s)).getOrElse(JsNull)))
        }
      )
    ),
    "atlas"       -> FirstElement(ValueList("parcellationAtlas")),
    "region"      -> ObjectList("parcellationRegion", Url("url", OrElse(Value("alias"), Value("name")))),
    "preparation" -> FirstElement(ValueList("preparation")),
    "methods"     -> ValueList("methods"),
    "protocol"    -> ValueList("protocol"),
    "viewer" ->
    OrElse(
      ObjectList(
        "brainviewer",
        Url("url", Value("name", js => JsString("Show " + js.as[String] + " in brain atlas viewer")))
      ),
      ObjectList(
        "neuroglancer",
        Url("url", Value("title", js => JsString("Show " + js.as[String] + " in brain atlas viewer")))
      )
    ),
    "subjects" -> ObjectList(
      "subjects",
      ObjectValue(
        List(
          NestedObject(
            "subject_name",
            Reference(
              "identifier",
              Value("name"),
              TemplateHelper.refUUIDToSearchId("Subject")
            )
          ),
          NestedObject("species", FirstElement(ValueList("species"))),
          NestedObject("sex", FirstElement(ValueList("sex"))),
          NestedObject("age", Value("age")),
          NestedObject("agecategory", FirstElement(ValueList("agecategory"))),
          NestedObject("weight", Value("weight")),
          NestedObject("strain", Optional(Value("strain"))),
          NestedObject("genotype", Value("genotype")),
          NestedObject(
            "samples",
            ObjectList("samples", Reference("identifier", Value("name"), TemplateHelper.refUUIDToSearchId("Sample")))
          ),
        ): _*
      ),
      js => {
        val res = for {
          obj <- js.asOpt[JsObject]
        } yield {
          Map("children" -> obj)
        }
        Json.toJson(res)
      }
    ),
    "first_release" -> Value("first_release"),
    "last_release"  -> Value("last_release"),
  )

}
