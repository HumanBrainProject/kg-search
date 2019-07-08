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
package models.excel

import constants.SchemaFieldsConstants
import helpers.NexusHelper
import helpers.excel.ExcelUnimindsImportHelper
import models.NexusPath
import models.excel.Entity._
import monix.eval.Task
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import play.api.libs.json._
import services.NexusService

import scala.collection.immutable.HashSet

case class Entity(
  rawType: String,
  localId: String,
  rawContent: Map[String, Value],
  path: Option[NexusPath] = None,
  status: Option[String] = None
) {
  def `type` = rawType.toLowerCase.trim

  def buildValidKey(key: String): String = {
    key.trim.replace(" ", "_")
  }

  def externalId: Option[String] = rawContent.get(ID_LABEL) match {
    case Some(SingleValue(idValue, _, _, _)) =>
      if (isNexusLink(idValue)) Some(idValue) else None
    case _ => None
  }

  /* filter empty content from raw content */
  def content = rawContent.-(ID_LABEL).flatMap {
    case (key, value) =>
      value.getNonEmtpy() match {
        case Some(nonEmptyValue) => Some((buildValidKey(key), nonEmptyValue))
        case None                => None
      }
  }

  def toJson(): JsObject = {
    JsObject(
      Map(
        "type"             -> JsString(`type`),
        "id"               -> JsString(localId),
        "externalId"       -> JsString(externalId.getOrElse(EMPTY_LINK)),
        "content"          -> JsObject(content.mapValues(_.toJson())),
        "insertion_status" -> JsString(status.getOrElse(Value.DEFAULT_RESOLUTION_STATUS))
      )
    )
  }

  def toJsonLd(): JsObject = {
    val originalContent = content.flatMap {
      case (key, value) =>
        if (isLink(key)) {
          val jsonBlock = value.toJsonLd()
          jsonBlock match {
            case JsNull => None
            case _ =>
              this.path match {
                case Some(p) =>
                  Some((s"http://hbp.eu/${ExcelUnimindsImportHelper.unimindsOrg}/${p.schema.capitalize}", jsonBlock))
                case None => None
              }
          }
        } else {
          Some((key, value.toJsonLd()))
        }
    }.toSeq
    val identifier = JsString(NexusHelper.hash(s"${`type`}$localId"))
    val schemaType = path match {
      case Some(p) => JsString(s"http://hbp.eu/${ExcelUnimindsImportHelper.unimindsOrg}/${p.schema.capitalize}")
      case None    => JsString("")
    }
    JsObject(
      originalContent :+
      ("@type", schemaType) :+
      (SchemaFieldsConstants.IDENTIFIER, identifier)
    )
  }

  def resolveLinks(linksRef: collection.mutable.Map[String, String]): Entity = {
    val newContent = content.map {
      case (key, value) =>
        if (isLink(key)) {
          (key, value.resolveValue(linksRef))
        } else {
          (key, value)
        }
    }
    copyWithID(newContent)
  }

  def validateLinksAndStatus(
    entityLink: Option[String],
    newStatus: Option[String] = None,
    token: String,
    nexusService: NexusService
  ): Task[Entity] = {
    // links validation
    val validatedContentFut = content.foldLeft(Task.pure(Map.empty[String, Value])) {
      case (contentFut, (key, value)) =>
        contentFut.flatMap(
          content =>
            if (isLink(key)) {
              value.validateValue(token, nexusService).map(value => content.+((key, value)))
            } else {
              Task.pure(content.+((key, value)))
          }
        )
    }

    // indirect externalId update if needed
    validatedContentFut.map { validatedContent =>
      val newContent = entityLink match {
        case Some(idLink) =>
          validatedContent + ((ID_LABEL, SingleValue(idLink)))
        case None =>
          validatedContent
      }
      copyWithID(newContent, newStatus)
    }
  }

  def checkInternalLinksValidity(dataRef: HashSet[String]): Entity = {
    val checkedContent = content.map {
      case (key, value) =>
        if (isLink(key)) {
          (key, value.checkInternalLinksValidity(dataRef))
        } else {
          (key, value)
        }
    }
    copyWithID(checkedContent)
  }

  def getExternalIdAsSingleValue: SingleValue = {
    SingleValue(externalId.getOrElse(EMPTY_LINK), None, status)
  }

  /* insert entity data at idx row and return index of next row to be used */
  def toExcelRows(sheet: XSSFSheet, idx: Int, styleOpt: Option[CellStyle] = None): Int = {
    content.toSeq
      .sortWith(_._1 < _._1)
      .+:((ID_LABEL, getExternalIdAsSingleValue))
      .foldLeft(0) {
        case (count, (key, value)) =>
          val valuesString = value.toStringSeq()
          valuesString.foldLeft(0) {
            case (subcount, (valueString, unit, status, label)) =>
              val row = sheet.createRow(idx + count + subcount)
              Seq(`type`, localId, key, label, valueString, unit, status).zipWithIndex.foreach {
                case (value, colIdx) =>
                  val cell = row.createCell(colIdx)
                  cell.setCellValue(value)
                  styleOpt.map(cell.setCellStyle(_))
              }
              subcount + 1
          } + count
      } + idx
  }

  /* header is: block name | block id |	key |	value |	unit of value | status */
  def toCsv(): Seq[String] = {
    content.toSeq
      .sortWith(_._1 < _._1)
      .+:((ID_LABEL, getExternalIdAsSingleValue))
      .flatMap {
        case (key, value) =>
          val valuesString = value.toStringSeq()
          valuesString.map {
            case (valueString, unit, status, label) =>
              s"${Seq(`type`, localId, key, label, valueString, unit, status).mkString(CSV_SEP)}\n"
          }
      }
  }

  /* copy entity with a new rawContent ensuring externalId, computed from rawContent, is not lost */
  def copyWithID(newContent: Map[String, Value], statusOpt: Option[String] = None): Entity = {
    val newStatus = statusOpt.orElse(status)

    newContent.get(ID_LABEL) match {
      case None if externalId != None =>
        this.copy(rawContent = newContent.+((ID_LABEL, getExternalIdAsSingleValue)), status = newStatus)
      case _ =>
        this.copy(rawContent = newContent, status = newStatus)
    }
  }

  def addContent(key: String, newValue: String, unit: Option[String] = None, label: String = ""): Entity = {
    SingleValue(newValue, unit, label = label).getNonEmtpy() match {
      case Some(nonEmptyValue) =>
        val newContent = content.get(key) match {
          case Some(value) =>
            content + (key -> value.addValue(newValue, unit, label = label))
          case None =>
            content + (key -> nonEmptyValue)
        }
        copyWithID(newContent)
      case None => this
    }
  }

  def isLink(key: String): Boolean = {
    key.startsWith(LINK_PREFIX)
  }

  def getInternalLinkedIds(): Seq[String] = {
    content
      .collect {
        case (key, value) if isLink(key) =>
          value.toStringSeq().map(_._1).filterNot(isNexusLink)
      }
      .flatten
      .toSeq
  }
}

object Entity {
  val LINK_PREFIX = "_"
  val ID_LABEL = "_ID"
  val EMPTY_LINK = "NA"
  val CSV_SEP = "^"

  def isNexusLink(value: String): Boolean = {
    value.matches(
      "^http[s]://nexus.*?.humanbrainproject.org/v\\d*/data/.*?/.*?/.*?/v\\d*?.\\d*?.\\d*?/[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}"
    )
  }
}
