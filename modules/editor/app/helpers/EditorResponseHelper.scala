
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
package helpers

import akka.util.ByteString
import models.NexusPath
import play.api.http.HttpEntity
import play.api.http.Status.{FORBIDDEN, UNAUTHORIZED}
import play.api.libs.ws.WSResponse
import play.api.mvc.{ResponseHeader, Result}
import services.specification.FormService

object EditorResponseHelper {
  /**
    * This function returns a Play Result with a back link
    *
    * @param status           The status of the response
    * @param headers          The headers of the response
    * @param errorMsg         The body of the response (could be either a String or JsValue)
    * @return A Play result with back link
    */
  def errorResult(
                               status: Int,
                               headers: Map[String, Seq[String]],
                               errorMsg: Any
                             ): Result = {
    if (status == UNAUTHORIZED) {
      Result(
        ResponseHeader(
          FORBIDDEN,
          ResponseHelper.flattenHeaders(ResponseHelper.filterContentTypeAndLengthFromHeaders[Seq[String]](headers))
        ),
        HttpEntity.Strict(ByteString(errorMsg.toString()), Some("application/json"))
      )
    } else {
      Result(
        ResponseHeader(
          status,
          ResponseHelper.flattenHeaders(ResponseHelper.filterContentTypeAndLengthFromHeaders[Seq[String]](headers))
        ),
        HttpEntity.Strict(ByteString(errorMsg.toString()), Some("application/json"))
      )
    }
  }

  /**
    * This function forward a reponse as a Play Result
    *
    * @param res The response to be forwarded
    * @return A result reflecting the response
    */
  def forwardResultResponse(res: WSResponse): Result = {
    Result(
      ResponseHeader(res.status, ResponseHelper.flattenHeaders(ResponseHelper.filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
      HttpEntity.Strict(res.bodyAsBytes, ResponseHelper.getContentType(res.headers))
    )
  }
}
