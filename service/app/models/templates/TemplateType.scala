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
package models.templates

import play.api.mvc.PathBindable

sealed trait TemplateType

object TemplateType {

  def apply(s: String): TemplateType = s.toUpperCase match {
    case "DATASET" => Dataset
    case "PERSON" => Person
    case "PROJECT" => Project
  }

  def toSchema(templateType: TemplateType): String = templateType match {
    case Dataset => "minds/core/dataset/v1.0.0"
    case Person => "minds/core/person/v1.0.0"
    case Project => "minds/core/placomponent/v1.0.0"
  }

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[TemplateType] = new PathBindable[TemplateType] {
    override def bind(key: String, value: String): Either[String, TemplateType] = {
      for {
        str <- stringBinder.bind(key, value).right
      } yield TemplateType(str)
    }

    override def unbind(key: String, templateType: TemplateType): String = {
      templateType.toString
    }
  }
}

case object Dataset extends TemplateType

case object Person extends TemplateType

case object Project extends TemplateType
