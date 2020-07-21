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

import helpers.ESHelper
import play.api.mvc.{PathBindable, QueryStringBindable}

sealed trait DatabaseScope {
  def toIndexName: String
}

object DatabaseScope {

  def apply(s: String): DatabaseScope = s.toUpperCase match {
    case "INFERRED" => INFERRED
    case "RELEASED" => RELEASED
  }

  implicit def queryBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[DatabaseScope] =
    new QueryStringBindable[DatabaseScope] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, DatabaseScope]] = {
        for {
          str <- stringBinder.bind("databaseScope", params)
        } yield
          (str match {
            case Right(dbScope) => Right(DatabaseScope(dbScope))
            case _              => Left("Unable to process database scope")
          })
      }

      override def unbind(key: String, databaseScope: DatabaseScope): String = {
        databaseScope.toString
      }
    }
}

case object INFERRED extends DatabaseScope {
  override def toString: String = "INFERRED"

  override def toIndexName: String = ESHelper.curatedIndexPrefix
}

case object RELEASED extends DatabaseScope {
  override def toString: String = "RELEASED"

  override def toIndexName: String = ESHelper.publicIndexPrefix
}
