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
import helpers.ESHelper
import models.user.{Group, IDMUser}
import models.{AccessToken, BasicAccessToken, MindsGroupSpec, Pagination, RefreshAccessToken, UserGroup}
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.cache.{AsyncCacheApi, NamedCache}
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status._
import play.api.libs.json.{JsPath, JsValue}
import play.api.libs.ws.{WSClient, WSResponse}

class IDMAPIService @Inject()(
  WSClient: WSClient,
  config: ConfigurationService,
  esService: ESService,
  @NamedCache("userinfo-cache") cache: AsyncCacheApi
)(
  implicit OIDCAuthService: TokenAuthService,
  clientCredentials: CredentialsService
) {
  private val log = LoggerFactory.getLogger(this.getClass)
  object cacheService extends CacheService

  def getUserInfoFromID(userId: String, token: AccessToken): Task[Option[IDMUser]] = {
    if (userId.isEmpty) {
      Task.pure(None)
    } else {
      val url = s"${config.idmApiEndpoint}/user/$userId"
      val q = WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token.token)

      val queryResult = token match {
        case BasicAccessToken(_)   => Task.deferFuture(q.get())
        case RefreshAccessToken(_) => AuthHttpClient.getWithRetry(q)
      }
      for {
        userRes    <- queryResult
        userGroups <- getUserGroups(userId, token)
        user = userRes.json.asOpt[IDMUser] match {
          case Some(u) => Some(u.copy(groups = userGroups, isCurator = isUserPartOfGroups(u, List("nexus-curators"))))
          case None    => None
        }
      } yield user
    }
  }

  def getUserInfo(token: BasicAccessToken): Task[Option[IDMUser]] = {
    getUserInfoWithCache(token)
  }

  private def getUserInfoWithCache(token: BasicAccessToken): Task[Option[IDMUser]] = {
    cacheService.getOrElse[IDMUser](cache, token.token) {
      getUserInfoFromToken(token).map {
        case Some(userInfo) =>
          cacheService.set[IDMUser](cache, token.token, userInfo, config.cacheExpiration)
          Some(userInfo)
        case _ =>
          None
      }
    }
  }

  private def getUserInfoFromToken(token: BasicAccessToken): Task[Option[IDMUser]] = {
    val userRequest = WSClient.url(s"${config.idmApiEndpoint}/user/me").addHttpHeaders(AUTHORIZATION -> token.token)
    Task.deferFuture(userRequest.get()).flatMap { res =>
      res.status match {
        case OK =>
          res.json.asOpt[IDMUser] match {
            case Some(u) =>
              getUserGroups(u.id, token).map { groups =>
                Some(u.copy(groups = groups))
              }
            case None => Task.pure(None)
          }
        case _ =>
          log.error(s"Could not fetch user - ${res.body}")
          Task.pure(None)
      }
    }
  }

  def getUsers(
    size: Int,
    searchTerm: String,
    token: AccessToken
  ): Task[Either[WSResponse, (List[IDMUser], Pagination)]] = {
    val url = s"${config.idmApiEndpoint}/user/search"
    if (!searchTerm.isEmpty) {
      Task
        .deferFuture {
          WSClient
            .url(url)
            .addHttpHeaders(AUTHORIZATION -> token.token)
            .addQueryStringParameters(
              "pageSize"    -> size.toString,
              "displayName" -> s"*$searchTerm*",
              "email"       -> s"*$searchTerm*",
              "username"    -> s"*$searchTerm*",
              "sort"        -> "displayName,asc"
            )
            .get()
        }
        .flatMap { res =>
          res.status match {
            case OK =>
              Task
                .gather(
                  (res.json \ "_embedded" \ "users")
                    .as[List[IDMUser]]
                    .map { u =>
                      hasUserGroups(u, List("nexus-curators"), token).map {
                        case Right(bool) => u.copy(isCurator = bool)
                        case Left(res) =>
                          log.error(s"Could not verify if user is curator - ${res.body}")
                          u
                      }
                    }
                )
                .map { users =>
                  val pagination = (res.json \ "page").as[Pagination]
                  Right(users, pagination)
                }
            case _ => Task.pure(Left(res))
          }
        }
    } else {
      Task.pure(Right(List(), Pagination.empty))
    }
  }

  private def isUserPartOfGroups(user: IDMUser, groups: List[String]): Boolean = {
    groups.forall(s => user.groups.exists(g => g.name.equals(s)))
  }

  def hasUserGroups(
    user: IDMUser,
    groups: List[String],
    token: AccessToken
  ): Task[Either[WSResponse, Boolean]] = {
    val url = s"${config.idmApiEndpoint}/user/${user.id}/member-groups"
    val queryGroups: List[(String, String)] = groups.map("name" -> _)
    val q = WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token.token).addQueryStringParameters(queryGroups: _*)
    val r = token match {
      case BasicAccessToken(_)   => Task.deferFuture(q.get())
      case RefreshAccessToken(_) => AuthHttpClient.getWithRetry(q)
    }
    r.map { res =>
      res.status match {
        case OK => Right((res.json \ "_embedded" \ "groups").as[List[JsValue]].nonEmpty)
        case _  => Left(res)
      }
    }

  }

  def getUserGroups(
    userId: String,
    token: AccessToken,
  ): Task[List[Group]] = {
    for {
      userGroups <- getGroups(userId, token)
    } yield userGroups
  }

  private def getGroups(userId: String, token: AccessToken): Task[List[Group]] = {
    val url = s"${config.idmApiEndpoint}/user/$userId/member-groups"
    val q =
      WSClient.url(url).withHttpHeaders(AUTHORIZATION -> token.token).addQueryStringParameters("pageSize" -> "1000")
    val r = token match {
      case BasicAccessToken(_)   => Task.deferFuture(q.get())
      case RefreshAccessToken(_) => AuthHttpClient.getWithRetry(q)
    }
    r.map { res =>
      res.status match {
        case OK =>
          (res.json \ "_embedded" \ "groups").as[List[Group]]
        case _ =>
          log.error(s"Could not fetch user groups - Status:${res.status} - Content:${res.body}")
          List()
      }
    }
  }

  /**
    * From a UserInfo object returns the index accessible in ES
    * @param userInfo The user info
    * @return A list of accessible index in ES
    */
  def groups(userInfo: Option[IDMUser]): Task[List[UserGroup]] = {
    userInfo match {
      case Some(info) =>
        for {
          esIndices <- esService.getEsIndices()
        } yield {
          val kgIndices = esIndices.filter(_.startsWith("kg_")).map(_.substring(3))
          val nexusGroups = info.groups
          val resultingGroups = ESHelper.filterNexusGroups(nexusGroups).filter(group => kgIndices.contains(group))
          log.debug(esIndices + "\n" + kgIndices + "\n " + nexusGroups)
          resultingGroups.toList.map { groupName =>
            if (MindsGroupSpec.group.contains(groupName)) {
              UserGroup(groupName, Some(MindsGroupSpec.v))
            } else {
              UserGroup(groupName, None)
            }
          }
        }
      case _ => Task.pure(List())
    }
  }

}
