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
package utils

import models.templates.entities.{
  CustomObject,
  EmptyEntity,
  NestedObject,
  ObjectValueList,
  ObjectValueMap,
  ReferenceObject,
  TemplateEntity,
  UrlObject,
  ValueObject,
  ValueObjectList,
}
import play.api.libs.json._

import scala.collection.Map

trait TemplateComponent {
  type T <: TemplateEntity
  def zero: T
  def op(content: Map[String, JsValue]): Option[T] = None
}

trait TemplateWriter extends TemplateComponent {
  override type T <: TemplateEntity

}

trait TemplateReader extends TemplateComponent {
  override type T <: TemplateEntity
}
trait IList extends TemplateWriter

case class CustomField[ReturnType: Format](fieldName: String, customField: String) extends TemplateWriter {
  override type T = CustomObject[ReturnType]
  override def op(content: Map[String, JsValue]): Option[CustomObject[ReturnType]] = {
    if (content.contains(fieldName)) {
      for {
        v <- content.get(fieldName)
      } yield CustomObject[ReturnType](customField, v.asOpt[ReturnType])
    } else {
      None
    }
  }
  override def zero: CustomObject[ReturnType] = CustomObject.zero[ReturnType]

}

case class Optional[A <: TemplateWriter](template: A) extends TemplateWriter {
  override type T = template.T
  override def op(content: Map[String, JsValue]): Option[T] = {
    template.op(content)
  }

  override def zero: template.T = template.zero
}

case class Value[ReturnType: Format](
  fieldName: String,
  transform: ValueObject[ReturnType] => ValueObject[ReturnType]
) extends TemplateWriter {
  override type T = ValueObject[ReturnType]

  override def op(content: Map[String, JsValue]): Option[ValueObject[ReturnType]] = {
    if (content.contains(fieldName)) {
      for {
        v <- content.get(fieldName)
      } yield transform(ValueObject[ReturnType](v.asOpt[ReturnType]))
    } else {
      None
    }
  }

  override def zero: ValueObject[ReturnType] = ValueObject.zero
}

case class Reference(
  fieldName: String,
  transform: ReferenceObject => ReferenceObject = s => s
) extends TemplateWriter {
  override type T = ReferenceObject
  override def op(content: Map[String, JsValue]): Option[ReferenceObject] = {
    if (content.contains(fieldName)) {
      for {
        v <- content.get(fieldName)
      } yield transform(ReferenceObject(v.asOpt[String]))
    } else {
      None
    }
  }
  override def zero: ReferenceObject = ReferenceObject.zero

}

case class Url(
  fieldName: String,
  transform: UrlObject => UrlObject = s => s
) extends TemplateWriter {
  override type T = UrlObject
  override def op(content: Map[String, JsValue]): Option[UrlObject] = {
    if (content.contains(fieldName)) {
      for {
        v <- content.get(fieldName)
      } yield transform(UrlObject(v.asOpt[String]))
    } else {
      None
    }
  }
  override def zero: UrlObject = UrlObject.zero

}

case class OrElse[Template <: TemplateWriter](left: Template, right: Template) extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[TemplateEntity] = {
    left.op(content) match {
      case v @ Some(_) => v
      case None        => right.op(content)
    }
  }

  override def zero: TemplateEntity = left.zero
}

case class Merge[T <: TemplateComponent, T2 <: TemplateComponent](
  templateLeft: T,
  templateRight: T2,
  merge: Option[TemplateEntity] => Option[TemplateEntity] => Option[TemplateEntity]
) extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[TemplateEntity] = {
    merge(templateLeft.op(content))(templateRight.op(content))
  }

  override def zero: TemplateEntity = templateLeft.zero
}

case class ValueList[ReturnType: Format](
  fieldName: String,
  transform: ValueObject[ReturnType] => ValueObject[ReturnType]
) extends IList {
  override type T = ValueObjectList[ReturnType]
  override def op(content: Map[String, JsValue]): Option[ValueObjectList[ReturnType]] = {
    content
      .get(fieldName)
      .flatMap { fieldValue =>
        val l = fieldValue
          .as[List[JsValue]]
          .map { el =>
            transform(ValueObject[ReturnType](el.asOpt[ReturnType]))
          }
        if (l.isEmpty) {
          None
        } else {
          Some(ValueObjectList(l))
        }
      }
  }

  override def zero = ValueObjectList.zero
}

case class ObjectValue[Template <: TemplateComponent](
  templates: List[Template],
  transform: ObjectValueMap => ObjectValueMap = identity
) extends TemplateWriter {
  override type T = ObjectValueMap
  override def op(content: Map[String, JsValue]): Option[ObjectValueMap] = {
    val resultList = templates.foldLeft(ObjectValueMap.zero) {
      case (l, opt @ Optional(_)) =>
        opt.op(content) match {
          case Some(content) => l :+ content
          case None          => l
        }
      case (l, template) =>
        template.op(content) match {
          case Some(content) => l :+ content
          case None          => l :+ template.zero
        }
    }
    Some(transform(resultList))
  }

  override def zero: ObjectValueMap = ObjectValueMap.zero
}

case class Nested[T <: TemplateWriter](fieldName: String, template: T) extends TemplateWriter {
  override type T = NestedObject
  override def op(content: Map[String, JsValue]): Option[NestedObject] = {
    template match {
      case opt @ Optional(_) =>
        opt.op(content).map(v => NestedObject(fieldName, v))
      case t => Some(NestedObject(fieldName, t.op(content).getOrElse(EmptyEntity())))
    }
  }

  override def zero: NestedObject = NestedObject.zero
}

case class FirstElement(list: IList) extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[T] = {
    list.op(content) match {
      case Some(ObjectValueList(l)) => l.headOption
      case Some(ValueObjectList(l)) => l.headOption
      case None                     => None
    }
  }

  override def zero: list.T = list.zero
}

case class ObjectListReader[A <: TemplateComponent](fieldName: String, el: A) extends TemplateReader with IList {
  override type T = ObjectValueList
  override def zero: ObjectValueList = ObjectValueList.zero
  override def op(content: Map[String, JsValue]): Option[ObjectValueList] = {
    val c = content
      .get(fieldName)
      .map { l =>
        val arr: List[JsObject] = l.asOpt[List[JsObject]] match {
          case Some(ll) => ll
          case None     => List(l.as[JsObject])
        }
        val updated = arr.foldLeft(ObjectValueList.zero) {
          case (l, js) =>
            el match {
              case opt @ Optional(_) =>
                opt.op(js.value) match {
                  case Some(entity) => l :+ entity
                  case None         => l
                }
              case template =>
                template.op(js.value) match {
                  case Some(entity) => l :+ entity
                  case None         => l :+ template.zero
                }
            }
        }
        if (updated.list.isEmpty) {
          None
        } else {
          Some(updated)
        }
      }
      .getOrElse(None)
    c
  }
}
