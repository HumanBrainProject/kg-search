package models

import CommonVars._

case class SpecimenGroup(id: String, subjects: Seq[Subject]) {

  // retrieve species from first subject species
  val globalSpecies = speciesMapping(subjects.head.details(speciesLabel).head)
  def toJsonString() = {
    val subjectsContent = subjects.map(_.toJsonString()).mkString(jsonSeparator)
    val innerSubjectsContent = Formatter.getJsonStringFromKV(subjectsLabel, Seq(globalSpecies), Some(subjectsContent))
    Formatter.getJsonStringFromKV(specimenGroupLabel, Seq(id), Some(innerSubjectsContent))
  }
}

