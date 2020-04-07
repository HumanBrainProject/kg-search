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
import models.templates.elasticSearch.DatasetMetaESTemplate
import models.templates.meta.DatasetMetaTemplate
import models.templates.instance.{DatasetTemplate, ModelInstanceTemplate, PersonTemplate, ProjectTemplate, SoftwareProjectTemplate, UnimindsPersonTemplate}
import models.templates.{Dataset, ModelInstance, Person, Project, SoftwareProject, Template, TemplateType, UnimindsPerson}
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.json.DefaultWrites
import utils._

import scala.collection.immutable.HashMap

@ImplementedBy(classOf[TemplateEngineImpl])
trait TemplateEngine[Content, TransformedContent] {
  def transform(c: Content, template: Template): TransformedContent

  def transformMeta(c: Content, template: Template): TransformedContent

  def getTemplateFromType(templateType: TemplateType, databaseScope: DatabaseScope): Template

  def getMetaTemplateFromType(templateType: TemplateType): Template

  def getESTemplateFromType(templateType: TemplateType): Template
}

class TemplateEngineImpl @Inject()(configuration: Configuration) extends TemplateEngine[JsValue, JsValue] {
  override def transform(c: JsValue, template: Template): JsValue = {
    val currentContent = c.as[JsObject].value
    val transformedContent = template.template.foldLeft(HashMap[String, JsValue]()) {
      case (acc, (k, v)) =>
        v match {
          case opt@Optional(_) =>
            opt.op(currentContent) match {
              case Some(content) => acc.updated(k, content.toJson)
              case None => acc
            }
          case _ =>
            acc.updated(k, v.op(currentContent).getOrElse(v.zero).toJson)
        }
    }
    val j = Json.toJson(transformedContent)
    j
  }

  private def fieldsListToMap(fieldList: List[JsObject]): Map[String, JsValue] = {
    fieldList.foldLeft(HashMap[String, JsValue]()) {
      case (acc, el) =>
        val maybeName = for {
          js <- el.value.get("fieldname")
          str <- js.asOpt[String]
        } yield str
        maybeName match {
          case Some(name) =>
            val fieldsMap = el.value.get("fields").map(fields => fieldsListToMap(fields.as[List[JsObject]]))
            val updatedFields = el.value.updated("fields", Json.toJson(fieldsMap))
            acc.updated(name, Json.toJson(updatedFields)(Writes.genericMapWrites))
          case None => acc
        }
    }
  }

  override def transformMeta(c: JsValue, template: Template): JsValue = {
    val maybeContent = for {
      fields <- c.as[JsObject].value.get("fields")
      fieldList <- fields.asOpt[List[JsObject]]
    } yield c.as[JsObject].value.updated("fields", Json.toJson(fieldsListToMap(fieldList)))
    maybeContent match {
      case Some(currentContent) =>
        val transformedContent = template.template.foldLeft(HashMap[String, JsValue]()) {
          case (acc, (k, v)) =>
            v match {
              case opt@Optional(_) =>
                opt.op(currentContent) match {
                  case Some(content) => acc.updated(k, content.toJson)
                  case None => acc
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
    case UnimindsPerson =>
      new UnimindsPersonTemplate {
        override def dataBaseScope: DatabaseScope = dbScope

        override def fileProxy: String = configuration.get[String]("file.proxy")
      }
    case ModelInstance =>
      new ModelInstanceTemplate {
        override def dataBaseScope: DatabaseScope = dbScope

        override def fileProxy: String = configuration.get[String]("file.proxy")
      }
    case SoftwareProject =>
      new SoftwareProjectTemplate  {
        override def dataBaseScope: DatabaseScope = dbScope

        override def fileProxy: String = configuration.get[String]("file.proxy")

      }
  }

  override def getMetaTemplateFromType(templateType: TemplateType): Template = templateType match {
    case Dataset => new DatasetMetaTemplate {}
    case _ => ???
  }

  override def getESTemplateFromType(templateType: TemplateType): Template = templateType match {
    case Dataset => new DatasetMetaESTemplate {}
    case _ => ???
  }
}
