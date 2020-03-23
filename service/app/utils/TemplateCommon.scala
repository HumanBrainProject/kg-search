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
  def op(content: Map[String, JsValue]): JsValue = JsNull
}

trait CustomField extends Template {
  def fieldName: String
  def transform: JsValue => JsValue
  def customField: String
  override def op(content: Map[String, JsValue]): JsValue = {
    if (content.contains(fieldName)) {
      Json.toJson(Map(customField -> content.get(fieldName).map(transform).getOrElse(JsNull)))
    } else {
      JsNull
    }
  }

}

trait IReference[T <: Template] extends CustomField {
  def valueField: T
  override def op(content: Map[String, JsValue]): JsValue = {
    val reference = content.get(fieldName).map(transform).getOrElse(JsNull)
    reference match {
      case JsNull => JsNull
      case ref =>
        val resultJsObj = Json.toJson(Map(customField -> ref)).as[JsObject]
        valueField.op(content).asOpt[JsObject].getOrElse(JsNull) match {
          case JsNull => resultJsObj
          case js     => resultJsObj ++ js.as[JsObject]
        }
    }

  }
}
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

case class Reference[T <: Template](
  override val fieldName: String,
  override val valueField: T,
  override val transform: JsValue => JsValue = identity
) extends IReference[T] {
  override def customField: String = "reference"

}

case class Url[T <: Template](
  override val fieldName: String,
  override val valueField: T,
  override val transform: JsValue => JsValue = identity
) extends IReference[T] {
  override def customField: String = "url"
}

case class OrElse[T <: Template](left: T, right: T) extends Template {
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
        val templateContent = template.op(content)
        templateContent match {
          case JsNull => jsObject
          case js     => jsObject ++ js.as[JsObject]
        }
    }
  }
}

case class NestedObject[T <: Template](fieldName: String, template: T) extends Template {
  override def op(content: Map[String, JsValue]): JsValue = {
    template match {
      case opt @ Optional(_) =>
        opt.getAsOpt(content) match {
          case Some(v) => Json.toJson(Map(fieldName -> v))
          case None    => JsNull
        }
      case t => Json.toJson(Map(fieldName -> t.op(content)))
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

case class ObjectList[A <: Template](fieldName: String, el: A, transform: JsValue => JsValue = identity) extends IList {
  override def op(content: Map[String, JsValue]): JsValue = {
    val c = content
      .get(fieldName)
      .map { l =>
        val arr: List[JsObject] = l.asOpt[List[JsObject]] match {
          case Some(ll) => ll
          case None     => List(l.as[JsObject])
        }
        val updated = arr.map(js => {
          val withTemplate = el.op(js.value)
          val transformed = transform(withTemplate)
          transformed
        })

        Json.toJson(updated)
      }
      .getOrElse(JsArray.empty)
    c
  }
}
