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
import models.Pagination
import org.slf4j.LoggerFactory
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class IDMAPIService @Inject()(WSClient: WSClient, config: ConfigurationService)(
  implicit executionContext: ExecutionContext
) {
  private val log = LoggerFactory.getLogger(this.getClass)

  def getUserInfoFromID(userId: String, token: String): Future[Option[IDMUser]] = {
    val url = s"${config.idmApiEndpoint}/user/$userId"
    WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token).get().flatMap { res =>
      res.status match {
        case OK =>
          val user = res.json.as[IDMUser]
          isUserPartOfGroups(user, List("nexus-curators"), token).map {
            case Right(bool) => Some(user.copy(isCurator = Some(bool)))
            case Left(r) =>
              log.error(s"Could not fetch user groups from IDM API ${r.body}")
              Some(user)
          }
        case _ => Future(None)
      }
    }
  }

  def getUsers(
    size: Int,
    searchTerm: String,
    token: String
  ): Future[Either[WSResponse, (List[IDMUser], Pagination)]] = {
    val url = s"${config.idmApiEndpoint}/user/searchByText"
    if (!searchTerm.isEmpty) {
      WSClient
        .url(url)
        .addHttpHeaders(AUTHORIZATION -> token)
        .addQueryStringParameters("pageSize" -> size.toString, "str" -> searchTerm)
        .get()
        .flatMap { res =>
          res.status match {
            case OK =>
              Future
                .sequence(
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
            case _ => Future(Left(res))
          }
        }
    } else {
      Future(Right(List(), Pagination.empty))
    }
  }

  def isUserPartOfGroups(user: IDMUser, groups: List[String], token: String): Future[Either[WSResponse, Boolean]] = {
    val url = s"${config.idmApiEndpoint}/user/${user.id}/member-groups"
    val queryGroups: List[(String, String)] = groups.map("name" -> _)
    WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token).addQueryStringParameters(queryGroups: _*).get().map {
      res =>
        res.status match {
          case OK => Right((res.json \ "_embedded" \ "groups").as[List[JsValue]].nonEmpty)
          case _  => Left(res)
        }
    }

  }

}
