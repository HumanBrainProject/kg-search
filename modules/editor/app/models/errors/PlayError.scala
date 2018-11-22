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

import akka.util.ByteString
import helpers.ResponseHelper
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import play.api.libs.ws.WSRequest
import play.api.mvc.{ResponseHeader, Result}

trait PlayError[T] {

  val status: Int
  val content: T

  def toJson: JsValue

  def toResult: Result = {
    Result(
      ResponseHeader(this.status),
      HttpEntity.Strict(ByteString(toJson.toString()), None)
    )
  }

}
