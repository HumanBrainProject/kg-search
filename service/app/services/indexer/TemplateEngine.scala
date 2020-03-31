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

import com.google.inject.ImplementedBy
import javax.inject.Inject
import models.DatabaseScope
import models.templates.instance.{DatasetTemplate, PersonTemplate, ProjectTemplate}
import models.templates.{Dataset, Person, Project, Template, TemplateType}
import play.api.Configuration
import play.api.libs.json._
import utils._

import scala.collection.immutable.HashMap

@ImplementedBy(classOf[TemplateEngineImpl])
trait TemplateEngine[Content, TransformedContent] {
  def transform(c: Content, template: Template): TransformedContent
  def transformMeta(c: Content, template: Template): TransformedContent

  def getTemplateFromType(templateType: TemplateType, databaseScope: DatabaseScope): Template
}

class TemplateEngineImpl @Inject()(configuration: Configuration) extends TemplateEngine[JsValue, JsValue] {
  override def transform(c: JsValue, template: Template): JsValue = {
    val currentContent = c.as[JsObject].value
    val transformedContent = template.template.foldLeft(HashMap[String, JsValue]()) {
      case (acc, (k, v)) =>
        v match {
          case opt @ Optional(_) =>
            opt.op(currentContent) match {
              case Some(content) => acc.updated(k, content.toJson)
              case None          => acc
            }
          case _ =>
            acc.updated(k, v.op(currentContent).getOrElse(v.zero).toJson)
        }
    }
    val j = Json.toJson(transformedContent)
    j
  }

  override def transformMeta(c: JsValue, template: Template): JsValue = {
    val maybeContent = for {
      fields    <- c.as[JsObject].value.get("fields")
      fieldList <- fields.asOpt[List[JsObject]]
    } yield
      fieldList.foldLeft(HashMap[String, JsValue]()) {
        case (acc, el) =>
          val maybeName = for {
            js  <- el.value.get("fieldname")
            str <- js.asOpt[String]
          } yield str
          maybeName match {
            case Some(name) => acc.updated(name, el)
            case None       => acc
          }
      }
    maybeContent match {
      case Some(currentContent) =>
        val transformedContent = template.template.foldLeft(HashMap[String, JsValue]()) {
          case (acc, (k, v)) =>
            v match {
              case opt @ Optional(_) =>
                opt.op(currentContent) match {
                  case Some(content) => acc.updated(k, content.toJson)
                  case None          => acc
                }
              case _ =>
                acc.updated(k, v.op(currentContent).getOrElse(v.zero).toJson)
            }
        }
        val j = Json.toJson(transformedContent)
        j
      case None => JsNull
    }
  }

  override def getTemplateFromType(templateType: TemplateType, dbScope: DatabaseScope): Template = templateType match {
    case Dataset =>
      new DatasetTemplate {
        override def dataBaseScope: DatabaseScope = dbScope
        override def fileProxy: String = configuration.get[String]("file.proxy")
      }
    case Person =>
      new PersonTemplate {
        override def dataBaseScope: DatabaseScope = dbScope
        override def fileProxy: String = configuration.get[String]("file.proxy")
      }
    case Project =>
      new ProjectTemplate {
        override def dataBaseScope: DatabaseScope = dbScope
        override def fileProxy: String = configuration.get[String]("file.proxy")
      }
  }
}
