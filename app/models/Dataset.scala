package models

import CommonVars._

case class Dataset(id: String, description: String, detailsRaw: Map[String, Seq[String]]) {

  val descriptionJson = Formatter.getJsonStringFromKV(descriptionLabel, Seq(Formatter.unquoteString(description)))

  def toJsonString(): String = {
    val innerDatasetContentJson = detailsRaw.map {
      case (key, value) =>
        key match {
          case `specimenGroupIdLabel` =>
            Formatter.getJsonStringFromKV(specimenGroupLabel, value)
          case `activityIdLabel` =>
            Formatter.getJsonStringFromKV(activityLabel, value)
          case `releaseDateLabel` =>
            // remove potential escape quotes set by excel formatter
            Formatter.getJsonStringFromKV(releaseDateLabel, value.map(_.replace("\"","")))
          case _ =>
            Formatter.getJsonStringFromKV(key, value)
        }
    }.mkString(jsonSeparator)
    val parcellationAtlasContent = Formatter.buildParcellationAtlasContent(Seq.empty, empty)
    val licenseIdSection = if (detailsRaw.contains(licenseIdLabel)) None else Some(Formatter.getJsonStringFromKV(licenseIdLabel, Seq(empty)))
    val formatSection = if (detailsRaw.contains(formatLabel)) None else Some(Formatter.getJsonStringFromKV(formatLabel, Seq(empty)))
    val filesSection = if (detailsRaw.contains(filesLabel)) None else Some(Formatter.getJsonStringFromKV(filesLabel, Seq(empty)))


    val content = Seq(Some(innerDatasetContentJson), Some(descriptionJson),Some(parcellationAtlasContent), filesSection, licenseIdSection, formatSection)
                 .flatten.mkString(jsonSeparator)
    Formatter.getJsonStringFromKV(datasetLabel, detailsRaw(datasetIdLabel),Some(content))
  }


}
