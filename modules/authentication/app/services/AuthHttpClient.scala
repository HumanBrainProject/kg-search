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

import models.AccessToken
import monix.eval.Task
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.http.Status.UNAUTHORIZED
import play.api.libs.ws.{BodyWritable, WSRequest, WSResponse}

object AuthHttpClient {

  def postWithRetry[T: BodyWritable](request: WSRequest, body: T)(
    implicit oIDCAuthService: TokenAuthService,
    credentials: CredentialsService
  ): Task[WSResponse] = {
    Task.deferFuture(request.post(body)).flatMap { res =>
      res.status match {
        case UNAUTHORIZED =>
          for {
            token <- oIDCAuthService.refreshAccessToken(credentials.getClientCredentials())
            res   <- Task.deferFuture(refreshToken(request, token).post(body))
          } yield res
        case _ => Task.pure(res)
      }
    }
  }

  def putWithRetry[T: BodyWritable](request: WSRequest, body: T)(
    implicit oIDCAuthService: TokenAuthService,
    credentials: CredentialsService
  ): Task[WSResponse] = {
    Task.deferFuture(request.put(body)).flatMap { res =>
      res.status match {
        case UNAUTHORIZED =>
          for {
            token <- oIDCAuthService.refreshAccessToken(credentials.getClientCredentials())
            res   <- Task.deferFuture(refreshToken(request, token).put(body))
          } yield res
        case _ => Task.pure(res)
      }
    }
  }

  def getWithRetry(request: WSRequest)(
    implicit oIDCAuthService: TokenAuthService,
    credentials: CredentialsService
  ): Task[WSResponse] = {
    Task.deferFuture(request.get()).flatMap { res =>
      res.status match {
        case UNAUTHORIZED =>
          for {
            token <- oIDCAuthService.refreshAccessToken(credentials.getClientCredentials())
            res   <- Task.deferFuture(refreshToken(request, token).get())
          } yield res
        case _ => Task.pure(res)
      }
    }
  }

  def deleteWithRetry(request: WSRequest)(
    implicit oIDCAuthService: TokenAuthService,
    credentials: CredentialsService
  ): Task[WSResponse] = {
    Task.deferFuture(request.delete()).flatMap { res =>
      res.status match {
        case UNAUTHORIZED =>
          for {
            token <- oIDCAuthService.refreshAccessToken(credentials.getClientCredentials())
            res   <- Task.deferFuture(refreshToken(request, token).delete())
          } yield res
        case _ => Task.pure(res)
      }
    }
  }

  def refreshToken(request: WSRequest, refreshedToken: AccessToken): WSRequest = {
    val headers = request.headers.updated(AUTHORIZATION, Seq(refreshedToken.token))
    request.withHttpHeaders(headers.map(l => l._1 -> l._2.mkString(" ")).toList: _*)
  }
}
