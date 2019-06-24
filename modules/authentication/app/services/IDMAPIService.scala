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
import models.user.IDMUser
import models.{AccessToken, BasicAccessToken, Pagination, RefreshAccessToken}
import monix.eval.Task
import org.slf4j.LoggerFactory
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}

class IDMAPIService @Inject()(WSClient: WSClient, config: ConfigurationService)(
  implicit OIDCAuthService: OIDCAuthService,
  clientCredentials: CredentialsService
) {
  private val log = LoggerFactory.getLogger(this.getClass)

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
      queryResult.flatMap { res =>
        res.status match {
          case OK =>
            val user = res.json.as[IDMUser]
            isUserPartOfGroups(user, List("nexus-curators"), token).map {
              case Right(bool) => Some(user.copy(isCurator = Some(bool)))
              case Left(r) =>
                log.error(s"Could not fetch user groups from IDM API ${r.body}")
                Some(user)
            }
          case _ => Task.pure(None)
        }
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
                      isUserPartOfGroups(u, List("nexus-curators"), token).map {
                        case Right(bool) => u.copy(isCurator = Some(bool))
                        case Left(_)     => u
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

  def isUserPartOfGroups(
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

}
