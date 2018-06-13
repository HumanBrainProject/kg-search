package models

import CommonVars._
case class Sample(id: String, detailsRaw: Map[String, Seq[String]]){

  // remove undersocres prefix
  val details = detailsRaw.map {
    case (k, v) => (k, v.map(_.replaceAll("^_* ","")))
  }

  def toJsonString() = {
    val fullId = s""""${details(subjectIdLabel).head}.${details(sampleIdLabel).head}""""
    val regionContent = Formatter.getJsonStringFromKV(regionLabel, details(regionLabel), listAsObject = true)
    val innerContent = details.flatMap {
      case (key, value) =>
        key match {
          case `regionLabel` | `sampleIdLabel` | `subjectIdLabel` => None
          case `parcellationAtlasLabel` =>
            Some(Formatter.getJsonStringFromKV(key, value, Some(regionContent)))
          case `dataPathLabel` =>
            Some(Formatter.getJsonStringFromKV(filesLabel, value))
          case _ =>
            Some(Formatter.getJsonStringFromKV(key, value))
        }
    }.mkString(",")
    Formatter.getJsonStringFromKV(sampleLabel, fullId, Some(innerContent))
  }
}
