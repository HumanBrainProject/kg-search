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

package common.services

import common.helpers.ESHelper
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class ESService @Inject()(wSClient: WSClient, configuration: ConfigurationService)(implicit ec: ExecutionContext) {

  def getEsIndices(): Future[List[String]] = {
    wSClient.url(configuration.esHost + s"/${ESHelper.indicesPath}?format=json").get().map { res =>
      val j = res.json
      j.as[List[JsValue]].map(json =>
        (json \ "index").as[String]
      )
    }
  }

}
