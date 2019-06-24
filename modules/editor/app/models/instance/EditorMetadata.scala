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
package models.instance

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{Format, JsNull, JsObject, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

final case class EditorMetadata(
  createdBy: String,
  createdAt: Option[DateTime],
  lastUpdateBy: String,
  lastUpdateAt: Option[DateTime],
  numberOfEdits: Long
)

object EditorMetadata {

  implicit val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString())
  }

  implicit val jodaDateReads: Reads[DateTime] = new Reads[DateTime] {

    def reads(d: JsValue): JsResult[DateTime] =
      JsSuccess(
        DateTimeFormat
          .forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
          .parseDateTime(d.as[String])
      )
  }

  implicit def optionFormat[T: Format]: Format[Option[T]] = new Format[Option[T]] {
    override def reads(json: JsValue): JsResult[Option[T]] = json.validateOpt[T]

    override def writes(o: Option[T]): JsValue = o match {
      case Some(t) ⇒ implicitly[Writes[T]].writes(t)
      case None ⇒ JsNull
    }
  }

  implicit val responseWithMetaDataWrites: Writes[EditorMetadata] = new Writes[EditorMetadata] {

    def writes(e: EditorMetadata): JsValue = {
      val valuesToBeAdded: List[(String, JsObject)] = List(
        "lastUpdateAt" -> Json.obj("label" -> "Last update at", "value" -> e.lastUpdateAt),
        "createdAt"    -> Json.obj("label" -> "Created at", "value"     -> e.createdAt)
      )
      val initialValues: JsObject = Json.obj(
        "createdBy"    -> Json.obj("label" -> "Created by", "value"     -> e.createdBy),
        "lastUpdateBy" -> Json.obj("label" -> "Last update by", "value" -> e.lastUpdateBy)
      )
      valuesToBeAdded.foldLeft(initialValues) {
        case (acc, value) =>
          if (value._2.value.get("value").map(_.as[Option[DateTime]]).getOrElse(None).isEmpty) {
            acc
          } else {
            acc + value
          }
      }
    }
  }

  def empty: EditorMetadata = EditorMetadata("", None, "", None, 0)
}
