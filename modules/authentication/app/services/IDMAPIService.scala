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
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.OK
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class IDMAPIService @Inject()(WSClient: WSClient, config: ConfigurationService)(
  implicit executionContext: ExecutionContext
) {

  def getUserInfoFromID(userId: String, token: String): Future[Option[IDMUser]] = {
    val url = s"${config.idmApiEndpoint}/user/$userId"
    WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token).get().map { res =>
      res.status match {
        case OK => Some(res.json.as[IDMUser])
        case _  => None
      }
    }
  }

  def getUsers(
    size: Int,
    from: Int,
    searchTerm: String,
    token: String
  ): Future[Either[WSResponse, (List[IDMUser], Pagination)]] = {
    val url = s"${config.idmApiEndpoint}/user/searchByText?page=$from&pageSize=$size&str=$searchTerm"
    WSClient.url(url).addHttpHeaders(AUTHORIZATION -> token).get().map { res =>
      res.status match {
        case OK =>
          val users = (res.json \ "_embedded" \ "users").as[List[IDMUser]]
          val pagination = (res.json \ "page").as[Pagination]
          Right(users, pagination)
        case _ => Left(res)
      }
    }
  }

}
