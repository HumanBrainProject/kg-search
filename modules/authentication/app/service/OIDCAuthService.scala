
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
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.{Configuration, Logger}
import play.api.http.Status._
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient
import play.api.mvc.Headers
import services.ESService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class OIDCAuthService @Inject()(
                                 config: Configuration,
                                 eSService: ESService,
                                 @NamedCache("userinfo-cache") cache: AsyncCacheApi,
                                 ws: WSClient
                               )(implicit ec: ExecutionContext) extends AuthService {
  val oidcUserInfoEndpoint = s"${config.get[String]("auth.endpoint")}/oidc/userinfo"
  val logger = Logger(this.getClass)
  val cacheExpiration = config.get[FiniteDuration]("proxy.cache.expiration")

  override type U = Option[UserInfo]

  /**
    * Fetch user info from cache or OIDC API
    * @param headers the header containing the user's token
    * @return An option with the UserInfo object
    */
  override def getUserInfo(headers: Headers): Future[Option[UserInfo]] = {
    val token = headers.get("Authorization").getOrElse("")
    getUserInfoWithCache(token)
  }


  /**
    * Query OIDC for user info
    * @param token The user's token
    * @return An option with the UserInfo object
    */
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

  /**
    * From a UserInfo object returns the index accessible in ES
    * @param userInfo The user info
    * @return A list of accessible index in ES
    */
  def groups(userInfo: Option[UserInfo]): Future[List[String]] = {
    userInfo match {
      case Some(info) =>
        for {
          esIndices <- eSService.getEsIndices()
        } yield {
          val kgIndices = esIndices.filter(_.startsWith("kg_")).map(_.substring(3))
          val nexusGroups =  info.groups
          val resultingGroups = ESHelper.filterNexusGroups(nexusGroups).filter(group => kgIndices.contains(group))
          logger.debug(esIndices + "\n" + kgIndices + "\n " + nexusGroups)
          resultingGroups.toList
        }
      case _ => Future.successful(List())
    }
  }

  /**
    * Fetch a UserInfo object from the cache or through the OIDC API
    * @param token The token from the user
    * @return An option with the UserInfo object
    */
  def getUserInfoWithCache(token: String): Future[Option[UserInfo]]  = {
    cache.get[UserInfo](token).flatMap{
      case Some(userInfo) =>
        logger.debug(s"User info fetched from cache ${userInfo.id}")
        Future.successful(Some(userInfo))
      case _ =>
        getUserInfoFromToken(token).map{
          case Some(userInfo) =>
            cache.set(token, userInfo, cacheExpiration)
            Some(userInfo)
          case _ =>
            None
        }
    }
  }
}
