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

case class SpecimenGroup(id: String, subjects: Seq[Subject]) {

  // retrieve species from first subject species
  val globalSpecies = speciesMapping(subjects.head.details(speciesLabel).head)

  def toJsonString() = {
    val subjectsContent = subjects.map(_.toJsonString()).mkString(jsonSeparator)
    val innerSubjectsContent = Formatter.getJsonStringFromKV(subjectsLabel, Seq(globalSpecies), Some(subjectsContent))
    Formatter.getJsonStringFromKV(specimenGroupLabel, Seq(id), Some(innerSubjectsContent))
  }
}
