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

case class Activity(id: String, detailsRaw: Map[String, Seq[String]]) {

  def toJsonString() = {
    val activityContentJsonString = detailsRaw
      .flatMap {
        case (key, value) =>
          key match {
            case `activityIdLabel` => None
            case _ =>
              Some(Formatter.getJsonStringFromKV(key, value))
          }

      }
      .mkString(jsonSeparator)

    val activityDescriptionContent = Formatter.getJsonStringFromKV(descriptionLabel, Seq(empty))
    val methodSection =
      if (detailsRaw.contains(methodLabel)) None else Some(Formatter.getJsonStringFromKV(methodLabel, Seq(empty)))
    val content = Seq(Some(activityContentJsonString), Some(activityDescriptionContent), methodSection).flatten
      .mkString(jsonSeparator)
    Formatter.getJsonStringFromKV(activityLabel, detailsRaw(activityIdLabel), Some(content))
  }
}
