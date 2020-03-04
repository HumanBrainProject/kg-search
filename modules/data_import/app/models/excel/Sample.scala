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

case class Sample(id: String, detailsRaw: Map[String, Seq[String]]) {

  // remove undersocres prefix
  val details = detailsRaw.map {
    case (k, v) => (k, v.map(_.replaceAll("^_* ", "")))
  }

  def toJsonString() = {
    val fullId = s""""${details(subjectIdLabel).head}.${details(sampleIdLabel).head}""""
    val regionContent = Formatter.getJsonStringFromKV(regionLabel, details(regionLabel), listAsObject = true)
    val innerContent = details
      .flatMap {
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
      }
      .mkString(",")
    Formatter.getJsonStringFromKV(sampleLabel, fullId, Some(innerContent))
  }
}
