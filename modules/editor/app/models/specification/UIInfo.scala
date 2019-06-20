/*
 *   Copyright (c) 2018, EPFL/Human Brain Project PCO
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

package models.specification

final case class UIInfo(labelField: String, promotedFields: List[String], promote: Option[Boolean])

object UIInfo {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val UIInfoReads: Reads[UIInfo] = (
    (JsPath \ "labelField").read[String] and
    (JsPath \ "promotedFields").read[List[String]] and
    (JsPath \ "promote").readNullable[Boolean]
  )(UIInfo.apply _)

  implicit val UIInfoWrites: Writes[UIInfo] = (
    (JsPath \ "labelField").write[String] and
    (JsPath \ "promotedFields").write[List[String]] and
    (JsPath \ "promote").writeNullable[Boolean]
  )(unlift(UIInfo.unapply))

}
