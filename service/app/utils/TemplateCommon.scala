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
  ESFields,
  ESKeyword,
  ESPropertiesObject,
  ESPropertyObject,
  ESValue,
  EmptyEntity,
  GetValue,
  ListOfObject,
  ListOfObjectWithValueField,
  NestedObject,
  ObjectMap,
  ObjectWithCustomField,
  ObjectWithReferenceField,
  ObjectWithUrlField,
  ObjectWithValueField,
  SetValue,
  TemplateEntity
}
import play.api.libs.json._

import scala.collection.Map
import scala.reflect.ClassTag

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
sealed trait IList extends TemplateWriter

case class PrimitiveToObjectWithCustomField[ReturnType: Format](
  fieldName: String,
  customField: String,
  transform: Option[ReturnType] => Option[ReturnType] = identity[Option[ReturnType]](_)
) extends TemplateWriter {
  override type T = ObjectWithCustomField[ReturnType]
  override def op(content: Map[String, JsValue]): Option[ObjectWithCustomField[ReturnType]] = {
    for {
      v <- content.get(fieldName)
    } yield ObjectWithCustomField[ReturnType](customField, transform(v.asOpt[ReturnType]))
  }
  override def zero: ObjectWithCustomField[ReturnType] = ObjectWithCustomField.zero[ReturnType]

}

case class Optional[A <: TemplateWriter](template: A) extends TemplateWriter {
  override type T = template.T
  override def op(content: Map[String, JsValue]): Option[T] = {
    template.op(content)
  }

  override def zero: template.T = template.zero
}

case class PrimitiveToObjectWithValueField[ReturnType: Format](
  fieldName: String,
  transform: ObjectWithValueField[ReturnType] => ObjectWithValueField[ReturnType] =
    identity[ObjectWithValueField[ReturnType]](_)
) extends TemplateWriter {
  override type T = ObjectWithValueField[ReturnType]

  override def op(content: Map[String, JsValue]): Option[ObjectWithValueField[ReturnType]] = {
    for {
      v <- content.get(fieldName)
    } yield transform(ObjectWithValueField[ReturnType](v.asOpt[ReturnType]))
  }

  override def zero: ObjectWithValueField[ReturnType] = ObjectWithValueField.zero
}

case class PrimitiveToObjectWithReferenceField(
  fieldName: String,
  transform: ObjectWithReferenceField => ObjectWithReferenceField = identity
) extends TemplateWriter {
  override type T = ObjectWithReferenceField
  override def op(content: Map[String, JsValue]): Option[ObjectWithReferenceField] = {
    for {
      v <- content.get(fieldName)
    } yield transform(ObjectWithReferenceField(v.asOpt[String]))
  }
  override def zero: ObjectWithReferenceField = ObjectWithReferenceField.zero

}

case class PrimitiveToObjectWithUrlField(
  fieldName: String,
  transform: ObjectWithUrlField => ObjectWithUrlField = identity
) extends TemplateWriter {
  override type T = ObjectWithUrlField
  override def op(content: Map[String, JsValue]): Option[ObjectWithUrlField] = {
    for {
      v <- content.get(fieldName)
    } yield transform(ObjectWithUrlField(v.asOpt[String]))
  }
  override def zero: ObjectWithUrlField = ObjectWithUrlField.zero

}

case class OrElse[Template <: TemplateComponent](left: Template, right: Template) extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[TemplateEntity] = {
    left.op(content) match {
      case Some(a) if a == left.zero => right.op(content)
      case None                      => right.op(content)
      case v                         => v
    }
  }

  override def zero: TemplateEntity = left.zero
}

case class Merge[T <: TemplateComponent, T2 <: TemplateComponent](
  templateLeft: T,
  templateRight: T2,
  merge: (Option[TemplateEntity], Option[TemplateEntity]) => Option[TemplateEntity]
) extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[TemplateEntity] = {
    merge(templateLeft.op(content), templateRight.op(content))
  }

  override def zero: TemplateEntity = templateLeft.zero
}

case class PrimitiveArrayToListOfValueObject[ReturnType: Format](
  fieldName: String,
  transform: ObjectWithValueField[ReturnType] => ObjectWithValueField[ReturnType] =
    identity[ObjectWithValueField[ReturnType]](_)
) extends IList {
  override type T = ListOfObjectWithValueField[ReturnType]
  override def op(content: Map[String, JsValue]): Option[ListOfObjectWithValueField[ReturnType]] = {
    content
      .get(fieldName)
      .flatMap { fieldValue =>
        val l = fieldValue
          .as[List[JsValue]]
          .map { el =>
            transform(ObjectWithValueField[ReturnType](el.asOpt[ReturnType]))
          }
        if (l.isEmpty) {
          None
        } else {
          Some(ListOfObjectWithValueField(l))
        }
      }
  }

  override def zero: ListOfObjectWithValueField[ReturnType] = ListOfObjectWithValueField.zero
}

case class WriteObject[Template <: TemplateComponent](
  templates: List[Template],
  transform: ObjectMap => ObjectMap = identity
) extends TemplateWriter {
  override type T = ObjectMap
  override def op(content: Map[String, JsValue]): Option[ObjectMap] = {
    val resultList = templates.foldLeft(ObjectMap.zero) {
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

  override def zero: ObjectMap = ObjectMap.zero
}

case class Nested[T <: TemplateComponent](fieldName: String, template: T) extends TemplateWriter {
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

case class FirstElement(list: IList, transform: Option[TemplateEntity] => Option[TemplateEntity] = identity)
    extends TemplateWriter {
  override type T = TemplateEntity
  override def op(content: Map[String, JsValue]): Option[T] = {
    list.op(content) match {
      case Some(ListOfObject(l))               => transform(l.headOption)
      case Some(ListOfObjectWithValueField(l)) => transform(l.headOption)
      case _                                   => None
    }
  }

  override def zero: list.T = list.zero
}

case class ObjectReader[A <: TemplateComponent](fieldName: String, el: A) extends TemplateReader {
  override type T = ObjectMap
  override def zero: ObjectMap = ObjectMap.zero

  override def op(content: Map[String, JsValue]): Option[ObjectMap] = {
    val c = content
      .get(fieldName)
      .flatMap { l =>
        l.asOpt[JsObject]
      }

    c match {
      case Some(v) => el.op(v.value).map(l => ObjectMap.zero :+ l)
      case None    => None
    }
  }
}

case class ObjectArrayToListOfObject[A <: TemplateComponent](
  fieldName: String,
  el: A,
  transform: Option[ListOfObject] => Option[ListOfObject] = identity
) extends TemplateReader
    with IList {
  override type T = ListOfObject
  override def zero: ListOfObject = ListOfObject.zero
  override def op(content: Map[String, JsValue]): Option[ListOfObject] = {
    val c = content
      .get(fieldName)
      .flatMap { l =>
        val arr: List[JsObject] = l.asOpt[List[JsObject]] match {
          case Some(ll) => ll
          case None     => List(l.as[JsObject])
        }
        val updated = arr.foldLeft(ListOfObject.zero) {
          case (l, js) =>
            checkOptional(l, js)
        }
        if (updated.list.isEmpty) {
          None
        } else {
          transform(Some(updated))
        }
      }
    c
  }

  private def checkOptional(l: ListOfObject, js: JsObject): ListOfObject = {
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
}

case class ESProperty(fieldName: String, fields: Option[ESFields] = Some(ESFields(ESKeyword())))
    extends TemplateWriter {
  override type T = ESPropertyObject
  override def zero: ESPropertyObject = ESPropertyObject.zero

  override def op(content: Map[String, JsValue]): Option[ESPropertyObject] = {
    val newType = for {
      t    <- content.get("searchUi:type")
      tStr <- t.asOpt[String]
    } yield tStr
    newType match {
      case Some(t) => Some(ESPropertyObject(fieldName, ESValue(`type` = t, fields = fields)))
      case _       => Some(ESPropertyObject(fieldName, ESValue(fields = fields)))
    }
  }
}

case class Set(fieldName: String, value: JsValue) extends TemplateWriter {
  override type T = SetValue

  override def zero: SetValue = SetValue.zero

  override def op(content: Map[String, JsValue]): Option[SetValue] = Some(SetValue(fieldName, value))
}

case class Get[ReturnType: Format](
  fieldName: String,
  transform: GetValue[ReturnType] => GetValue[ReturnType] = identity[GetValue[ReturnType]](_)
) extends TemplateWriter {
  override type T = GetValue[ReturnType]

  override def op(content: Map[String, JsValue]): Option[GetValue[ReturnType]] = {
    for {
      v  <- content.get(fieldName)
      vT <- v.asOpt[ReturnType]
    } yield transform(GetValue[ReturnType](Some(vT)))
  }

  override def zero: GetValue[ReturnType] = GetValue.zero
}
