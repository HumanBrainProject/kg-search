/*
 *   Copyright (c) 2019, EPFL/Human Brain Project PCO
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
import models.excel.CommonVars._

object Formatter {

  def getJsonStringFromKV(
    key: String,
    values: Seq[String],
    childrenValue: Option[String] = None,
    listAsObject: Boolean = false
  ): String = {
    val content = if (listAsObject) {
      values.map(Formatter.getJsonStringFromKV(_, "\"\"", None)).mkString(",")
    } else {
      values.map(v => s""""${escapeQuote(v).trim}"""").mkString(",")
    }
    val value = if (values.size <= 1) {
      s"""$content"""
    } else {
      s""" [ $content ] """
    }
    getJsonStringFromKV(key, value, childrenValue)
  }

  def getJsonStringFromKV(key: String, rawValue: String, childrenValueOpt: Option[String]): String = {
    // escape new line if quoted string content
    val value = if (rawValue.charAt(0) == '"') rawValue.replace("\n", "\\n") else rawValue
    val childrenValue = childrenValueOpt match {
      case Some(childrenVal) => s"""  "$childrenLabel": [ $childrenVal ], """
      case None              => ""
    }
    s"""
    {
      $childrenValue
      "$keyLabel": "$key",
      "$valueLabel": $value
    }
    """
  }

  def buildParcellationAtlasContent(regions: Seq[String], parcellationAtlas: String): String = {
    val regionContent = if (regions.isEmpty) {
      Formatter.getJsonStringFromKV(regionLabel, s"[]", None)
    } else {
      Formatter.getJsonStringFromKV(regionLabel, regions)
    }
    Formatter.getJsonStringFromKV(parcellationAtlasLabel, Seq(parcellationAtlas), Some(regionContent))
  }

  def escapeQuote(s: String): String = {
    s.replace("\"", "\\\"")
  }

  def unquoteString(content: String): String = {
    if (content.charAt(0) == '"' && content.last == '"') {
      content.substring(1, content.size - 1)
    } else {
      content
    }
  }
}
