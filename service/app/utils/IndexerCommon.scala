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
package utils

import play.api.libs.json._

import scala.collection.Map

trait Template {
  def op(content: Map[String, JsValue]): JsValue = { Json.obj() }
}

trait CustomField extends Template {
  def fieldName: String
  def transform: JsValue => JsValue
  def customField: String
  override def op(content: Map[String, JsValue]): JsValue = {
    Json.toJson(Map(customField -> content.get(fieldName).map(transform).getOrElse(JsNull)))
  }
}

trait IReference extends CustomField
trait IList extends Template

case class Optional(template: Template) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    template.op(content)
  }

  def getAsOpt(content: Map[String, JsValue]): Option[JsValue] = {
    this.op(content) match {
      case JsNull => None
      case js     => Some(js)
    }
  }
}

case class Value(override val fieldName: String, override val transform: JsValue => JsValue = identity)
    extends CustomField {
  override def customField: String = "value"
}

case class Reference(override val fieldName: String, override val transform: JsValue => JsValue = identity)
    extends IReference {
  override def customField: String = "reference"
}

case class Url(override val fieldName: String, override val transform: JsValue => JsValue = identity)
    extends IReference {
  override def customField: String = "url"
}

case class OrElse(left: Template, right: Template) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    left.op(content) match {
      case JsNull => right.op(content)
      case js     => js
    }
  }
}

case class Merge[T <: Template, T2 <: Template](
  templateLeft: T,
  templateRight: T2,
  merge: JsValue => JsValue => JsValue
) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    merge(templateLeft.op(content))(templateRight.op(content))
  }
}

case class ValueList(fieldName: String, transform: JsValue => JsValue = identity) extends IList {
  override def op(content: Map[String, JsValue]): JsValue = {
    content
      .get(fieldName)
      .map { fieldValue =>
        val l = fieldValue
          .as[List[JsValue]]
          .map { el =>
            val v = transform(el)
            Map("value" -> v)
          }
        Json.toJson(l)
      }
      .getOrElse(JsNull)
  }
}

case class EmptyValue(transform: Map[String, JsValue] => JsValue) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    Json.toJson(Map("value" -> transform(content)))
  }
}

case class DirectValue(fieldName: String, transform: JsValue => JsValue = identity) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = content.get(fieldName).map(transform).getOrElse(JsNull)
}

case class ObjectValue[T <: Template](templates: T*) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    templates.foldLeft(Json.obj()) {
      case (jsObject, opt @ Optional(_)) =>
        opt.getAsOpt(content) match {
          case Some(js) =>
            jsObject ++ js.as[JsObject]
          case None => jsObject
        }
      case (jsObject, template) =>
        jsObject ++ template.op(content).as[JsObject]
    }
  }
}

case class FirstElement(list: IList) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    val arr = list.op(content)
    val firstElement = for {
      jsArray <- arr.asOpt[JsArray]
      head    <- jsArray.value.headOption
    } yield head
    firstElement.getOrElse(JsNull)
  }
}

case class ObjectList[A <: Template](fieldName: String, el: A) extends IList {
  override def op(content: Map[String, JsValue]): JsValue = {
    val c = content
      .get(fieldName)
      .map { l =>
        val arr: List[JsObject] = l.asOpt[List[JsObject]] match {
          case Some(ll) => ll
          case None     => List(l.as[JsObject])
        }
        Json.toJson(arr.map(js => el.op(js.as[JsObject].value)))
      }
      .getOrElse(JsArray.empty)
    c
  }
}

case class Children(children: Map[String, Template])
