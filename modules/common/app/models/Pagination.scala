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
package models

import play.api.libs.json.{JsPath, Reads, Writes}

final case class Pagination(size: Int, total: Int, totalPage: Int, number: Int)

object Pagination {
  import play.api.libs.functional.syntax._
  implicit val previewInstanceReads: Reads[Pagination] = (
    (JsPath \ "size").read[Int] and
    (JsPath \ "totalElements").read[Int] and
    (JsPath \ "totalPages").read[Int] and
    (JsPath \ "number").read[Int]
  )(Pagination.apply _)

  implicit val previewInstanceWrites: Writes[Pagination] = (
    (JsPath \ "size").write[Int] and
    (JsPath \ "total").write[Int] and
    (JsPath \ "totalPages").write[Int] and
    (JsPath \ "number").write[Int]
  )(unlift(Pagination.unapply))

  def empty: Pagination = {
    Pagination(0, 0, 0, 0)
  }
}
