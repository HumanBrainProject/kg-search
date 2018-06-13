package models

import CommonVars._

object Formatter {

  def getJsonStringFromKV(key: String, values: Seq[String], childrenValue: Option[String] = None, listAsObject: Boolean = false): String = {
    val content = if (listAsObject){
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
    val value = rawValue.replace("\n", " ")
    val childrenValue = childrenValueOpt match {
      case Some(childrenVal) => s"""  "$childrenLabel": [ $childrenVal ], """
      case None => ""
    }
    s"""
    {
      $childrenValue
      "$keyLabel": "$key",
      "$valueLabel": $value
    }
    """
  }

  def buildParcellationAtlasContent(regions: Seq[String], parcellationAtlas: String):String = {
    val regionContent = if (regions.isEmpty){
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

