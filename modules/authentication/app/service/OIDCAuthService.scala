
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

package service.authentication

import com.google.inject.Inject
import common.helpers.ESHelper
import models.authentication.UserInfo
import play.api.{Configuration, Logger}
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import play.api.mvc.Headers
import services.ESService

import scala.concurrent.{ExecutionContext, Future}

class OIDCAuthService @Inject()(config: Configuration, eSService: ESService)(implicit ec: ExecutionContext, ws: WSClient) extends AuthService {
  val oidcUserInfoEndpoint = s"${config.get[String]("auth.endpoint")}/oidc/userinfo"
  val logger = Logger(this.getClass)

  override type U = Option[UserInfo]

  override def getUserInfo(headers: Headers): Future[Option[UserInfo]] = {
    val token = headers.get("Authorization").getOrElse("")
    getUserInfoFromToken(token)
  }

  def getUserInfoFromToken(token:String): Future[Option[UserInfo]] = {
    ws.url(oidcUserInfoEndpoint).addHttpHeaders("Authorization" -> token).get().map {
      res =>
        res.status match {
          case OK =>
            Some(UserInfo(res.json.as[JsObject]))
          case _ => None
        }
    }
  }

  def groups(userInfo: Option[UserInfo]): Future[List[String]] = {
    userInfo match {
      case Some(info) =>
        for {
          esIndices <- eSService.getEsIndices()
        } yield {
          val kgIndices = esIndices.filter(_.startsWith("kg_")).map(_.substring(3))
          val nexusGroups =  info.groups
          val resultingGroups = OIDCAuthService.extractNexusGroup(nexusGroups).filter(group => kgIndices.contains(group))
          logger.debug(esIndices + "\n" + kgIndices + "\n " + nexusGroups)
          resultingGroups.toList
        }
      case _ => Future.successful(List())
    }
  }
}

object OIDCAuthService {

  def extractNexusGroup(groups: Seq[String]): Seq[String] = {
      ESHelper.filterNexusGroups(groups)
  }
}