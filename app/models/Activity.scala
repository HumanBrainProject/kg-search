package models

import CommonVars._
case class Activity(id: String, detailsRaw: Map[String, Seq[String]]){

  def toJsonString() = {
    val activityContentJsonString = detailsRaw.flatMap {
      case (key, value) =>
        key match {
          case `activityIdLabel` => None
          case _ =>
            Some(Formatter.getJsonStringFromKV(key, value))
        }

    }.mkString(jsonSeparator)

    val activityDescriptionContent = Formatter.getJsonStringFromKV(descriptionLabel, Seq(empty))
    val methodSection = if (detailsRaw.contains(methodLabel)) None else Some(Formatter.getJsonStringFromKV(methodLabel, Seq(empty)))
    val content = Seq(Some(activityContentJsonString), Some(activityDescriptionContent), methodSection).flatten.mkString(jsonSeparator)
    Formatter.getJsonStringFromKV(activityLabel, detailsRaw(activityIdLabel), Some(content))
  }
}
