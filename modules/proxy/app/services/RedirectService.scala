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
package services

import com.google.inject.Inject
import monix.eval.Task
import play.api.libs.json.JsObject
import play.api.libs.ws.WSResponse
import play.twirl.api.HtmlFormat

class RedirectService @Inject()(esClient: ESService) {

  def renderHtml(dataType: String, id: String): Task[Either[WSResponse, HtmlFormat.Appendable]] = {
    esClient.getDataById(dataType, id).map {
      case Right(json) => Right(RedirectService.generateHtml(dataType, id, (json \ "_source").as[JsObject]))
      case Left(res)   => Left(res)
    }
  }
}

object RedirectService {

  def generateHtml(dataType: String, id: String, json: JsObject): HtmlFormat.Appendable = {
    views.html.searchResultTemplate(dataType, id, json.value)
  }
}
