/*
 *   Copyright (c) 2020, EPFL/Human Brain Project PCO
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

import play.api.mvc.PathBindable

sealed trait DatabaseScope {
  def toIndexName: String
}

object DatabaseScope {

  def apply(s: String): DatabaseScope = s.toUpperCase match {
    case "INFERRED" => INFERRED
    case "RELEASED" => RELEASED
  }

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[DatabaseScope] =
    new PathBindable[DatabaseScope] {
      override def bind(key: String, value: String): Either[String, DatabaseScope] = {
        for {
          str <- stringBinder.bind(key, value).right
        } yield DatabaseScope(str)
      }

      override def unbind(key: String, databaseScope: DatabaseScope): String = {
        databaseScope.toString
      }
    }
}

case object INFERRED extends DatabaseScope {
  override def toString: String = "INFERRED"

  override def toIndexName: String = "in_progress"
}

case object RELEASED extends DatabaseScope {
  override def toString: String = "RELEASED"

  override def toIndexName: String = "publicly_released"
}
