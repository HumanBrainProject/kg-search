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

case class Subject(id: String, details: Map[String, Seq[String]], samples: Seq[Sample]) {

  val globalSpecies = speciesMapping(details(speciesLabel).head)

  def toJsonString() = {
    val sampleInnerContent = samples.map(_.toJsonString()).mkString(",")
    val sampleContent = Formatter.getJsonStringFromKV(samplesLabel, Seq(globalSpecies), Some(sampleInnerContent))
    val innerContent = (details.flatMap {
      case (key, value) =>
        key match {
          case `subjectIdLabel` | `specimenGroupIdLabel` => None
          case _                                         => Some(Formatter.getJsonStringFromKV(key, value))
        }
    } ++ Seq(sampleContent)).mkString(jsonSeparator)

    Formatter.getJsonStringFromKV(subjectLabel, Seq(id), Some(innerContent))
  }
}
