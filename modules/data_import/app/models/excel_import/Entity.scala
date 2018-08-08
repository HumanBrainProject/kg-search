
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

package models.excel_import

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import Entity._
import data_import.helpers.excel_import.ExcelImportHelper
import play.libs.Json

case class Entity(rawType: String, id: String, rawContent: Map[String, Value]){
  def `type` = rawType.toLowerCase.trim

  // make key valid
  def buildValidKey(key: String): String = {
    key.trim.replace(" ", "_")
  }

  // filter empty content
  def content = rawContent.flatMap {
    case (key, value) => value.getNonEmtpy() match {
      case Some(nonEmptyValue) => Some((buildValidKey(key), nonEmptyValue))
      case None => None
    }
  }

  def toJson(): JsObject = {
    JsObject( Map(
      id -> JsObject.apply(
        content.map{
          case (key, value) => (key, value.toJson())
        }
      )
    ))
  }

  def toKGPayload(linksRef: collection.mutable.Map[String,String]): JsObject = {
    val originalContent = content.flatMap{
      case (key, value) =>
        if (isLink(key)) {
          val links = value.toStringSeq().flatMap(linksRef.get(_))
          if (links.isEmpty) {
            None
          } else {
            val linksBlock: JsValue = links.size match {
              case 1 =>
                JsObject(Map(
                  "@id" -> JsString(links.head)))
              case _ =>
                JsArray(
                  links.map(link => JsObject(Map("@id" -> JsString(link)))))
            }
            Some((s"http://hbp.eu/uniminds#$key", linksBlock))
          }
        } else {
          Some((key, value.toJson()))
        }
    }.toSeq
    val hashCode = JsString(ExcelImportHelper.hash(originalContent.toString()))
    val identifier = JsString(ExcelImportHelper.hash(s"${`type`}$id"))
    JsObject(
      originalContent :+
      ("@type", JsString(s"http://hbp.eu/uniminds#${`type`}")) :+
      ("http://schema.org/identifier", identifier) :+
      ("http://hbp.eu/internal#hashcode", hashCode)
    )
  }

  def addContent(key: String, newValue: String): Entity = {
    SingleValue(newValue).getNonEmtpy() match {
      case Some(nonEmptyValue) =>
        val newContent = content.get(key) match {
          case Some(value) =>
            content + (key -> value.addValue(newValue))
          case None =>
            content + (key -> nonEmptyValue)
        }
        this.copy(rawContent=newContent)
      case None => this
    }
  }

  def isLink(key: String): Boolean = {
    key.startsWith(link_prefix) && key != ID_LABEL
  }

  def getLinkedIds(): Seq[String] = {
    content.collect{
      case (key, value) if isLink(key) =>
          value.toStringSeq()
    }.flatten.toSeq
  }
}

object Entity {
  def link_prefix = "_"
  def ID_LABEL = "_ID"
  def EMPTY_LINK = "./"
}
