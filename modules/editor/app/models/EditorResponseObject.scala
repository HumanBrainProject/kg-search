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
package models

import constants.UiConstants
import play.api.libs.json._

trait EditorResponse {
  val data: JsValue
}

case class EditorResponseObject(override val data: JsValue) extends EditorResponse

object EditorResponseObject {
  import play.api.libs.functional.syntax._

  implicit val responseWrites: Writes[EditorResponseObject] =
    (JsPath \ UiConstants.DATA).write[JsValue].contramap(_.data)

  def empty: EditorResponseObject = EditorResponseObject(Json.obj())
}

case class EditorResponseWithCount(override val data: JsValue, label: String, total: Long) extends EditorResponse

object EditorResponseWithCount {

  import play.api.libs.functional.syntax._

  implicit val responseWithCountWrites: Writes[EditorResponseWithCount] = (
    (JsPath \ UiConstants.DATA).write[JsValue] and
    (JsPath \ "label").write[String] and
    (JsPath \ "total").write[Long]
  )(unlift(EditorResponseWithCount.unapply))

  def empty: EditorResponseWithCount = EditorResponseWithCount(JsArray(), "", 0)
}
