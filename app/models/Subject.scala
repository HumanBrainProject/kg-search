package models

import CommonVars._

case class Subject(id: String, details: Map[String, Seq[String]], samples: Seq[Sample]) {

  val globalSpecies = speciesMapping(details(speciesLabel).head)

  def toJsonString() = {
    val sampleInnerContent = samples.map(_.toJsonString()).mkString(",")
    val sampleContent = Formatter.getJsonStringFromKV(samplesLabel, Seq(globalSpecies), Some(sampleInnerContent))
    val innerContent = (details.flatMap {
      case (key, value) =>
        key match {
          case `subjectIdLabel` | `specimenGroupIdLabel`=> None
          case _ => Some(Formatter.getJsonStringFromKV(key, value))
        }
    } ++ Seq(sampleContent)).mkString(jsonSeparator)

    Formatter.getJsonStringFromKV(subjectLabel, Seq(id), Some(innerContent))
  }
}
