
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

package services

import java.io.FileInputStream

import com.google.inject._
import play.api.Configuration
import play.api.libs.json.Json

trait Credentials {
  def getClientCredentials(): ClientCredentials
}

case class ClientCredentials(refreshToken:String, clientId:String, clientSecret:String, openidHost: String)

@Singleton
class CredentialsService @Inject()(configuration: ConfigurationService) extends Credentials {
  var clientCredentials: Option[ClientCredentials] = None
  val fileName = s"${configuration.refreshTokenFile}/oidc"
  override def getClientCredentials(): ClientCredentials = {
    if(clientCredentials.isEmpty){

      val stream = new FileInputStream(fileName)
      try {
        val json = Json.parse(stream)
        clientCredentials = Some(ClientCredentials((json \ "refresh_token").as[String], (json \ "client_id").as[String], (json \ "client_secret").as[String], (json \ "openid_host").as[String] ))
        clientCredentials.get
      } finally {
        stream.close()
      }
    }else{
      clientCredentials.get
    }
  }
}
