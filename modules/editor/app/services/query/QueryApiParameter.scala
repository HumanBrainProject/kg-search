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

package services.query
import constants.QueryConstants
import constants.QueryConstants._
final case class QueryApiParameter(
  from: Option[Int] = None,
  size: Option[Int] = None,
  search: String = "",
  vocab: Option[String] = Some(QueryConstants.DEFAULT_VOCAB),
  databaseScope: Option[String] = None,
) {
  private def l =
    List(START -> from, SIZE -> size, SEARCH -> Some(search), VOCAB -> vocab, DATABASE_SCOPE -> databaseScope)

  def toParams: List[(String, String)] = {
    l.foldLeft(List[(String, String)]()) {
      case (acc, el) =>
        el._2 match {
          case Some(e) => (el._1, e.toString) :: acc
          case _       => acc
        }
    }
  }
}
