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

case class Dataset(id: String, description: String, detailsRaw: Map[String, Seq[String]]) {

  val descriptionJson = Formatter.getJsonStringFromKV(descriptionLabel, Seq(Formatter.unquoteString(description)))

  def toJsonString(): String = {
    val innerDatasetContentJson = detailsRaw
      .map {
        case (key, value) =>
          key match {
            case `specimenGroupIdLabel` =>
              Formatter.getJsonStringFromKV(specimenGroupLabel, value)
            case `activityIdLabel` =>
              Formatter.getJsonStringFromKV(activityLabel, value)
            case `releaseDateLabel` =>
              // remove potential escape quotes set by excel formatter
              Formatter.getJsonStringFromKV(releaseDateLabel, value.map(_.replace("\"", "")))
            case _ =>
              Formatter.getJsonStringFromKV(key, value)
          }
      }
      .mkString(jsonSeparator)
    val parcellationAtlasContent = Formatter.buildParcellationAtlasContent(Seq.empty, empty)
    val licenseIdSection =
      if (detailsRaw.contains(licenseIdLabel)) None else Some(Formatter.getJsonStringFromKV(licenseIdLabel, Seq(empty)))
    val formatSection =
      if (detailsRaw.contains(formatLabel)) None else Some(Formatter.getJsonStringFromKV(formatLabel, Seq(empty)))
    val filesSection =
      if (detailsRaw.contains(filesLabel)) None else Some(Formatter.getJsonStringFromKV(filesLabel, Seq(empty)))

    val content = Seq(
      Some(innerDatasetContentJson),
      Some(descriptionJson),
      Some(parcellationAtlasContent),
      filesSection,
      licenseIdSection,
      formatSection
    ).flatten.mkString(jsonSeparator)
    Formatter.getJsonStringFromKV(datasetLabel, detailsRaw(datasetIdLabel), Some(content))
  }

}
