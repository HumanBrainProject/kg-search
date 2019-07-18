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
package controllers

import com.google.inject.Inject
import models.user.IDMUser
import models.{AuthenticatedUserAction, BasicAccessToken, UserRequest}
import play.api.mvc.{BodyParsers, Request, Result}
import services.IDMAPIService

import scala.concurrent.{ExecutionContext, Future}

class TestAuthenticatedUserAction @Inject()(
  override val parser: BodyParsers.Default,
  authprovider: IDMAPIService,
  userInfo: IDMUser
)(implicit override val executionContext: ExecutionContext)
    extends AuthenticatedUserAction(parser, authprovider)(executionContext) {
  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    block(new UserRequest(userInfo, request, BasicAccessToken("token")))
  }

}
