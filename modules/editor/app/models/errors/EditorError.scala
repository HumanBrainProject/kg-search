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
package models.errors

import play.api.libs.json.{JsValue, Json}

trait APIEditorErrorInterface[T] extends PlayError[T] {
  override val status: Int
  override val content: T
}

final case class APIEditorError(override val status: Int, override val content: String)
    extends APIEditorErrorInterface[String] {

  override def toJson: JsValue = Json.obj(
    "error" -> Json.obj(
      "status"  -> status,
      "message" -> content
    )
  )

  override def toString: String = s"Status: $status - message: $content"
}

object APIEditorError {

  import play.api.libs.json._

  implicit val userListWrites: Writes[APIEditorError] = new Writes[APIEditorError] {

    def writes(error: APIEditorError): JsValue = {
      Json.obj(
        "error" -> Json.obj(
          "status"  -> error.status,
          "message" -> error.content
        )
      )
    }
  }
}

final case class APIEditorMultiError(override val status: Int, override val content: List[APIEditorError])
    extends APIEditorErrorInterface[List[_]] {

  override def toJson: JsValue = Json.obj(
    "error" -> Json.toJson(content.map(m => Json.obj("status" -> m.status, "message" -> m.content)))
  )
}

object APIEditorMultiError {

  def fromResponse(status: Int, content: String): APIEditorMultiError =
    APIEditorMultiError(status, List(APIEditorError(status, content)))
}
