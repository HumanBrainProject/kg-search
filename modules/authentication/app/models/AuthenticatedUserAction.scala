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

package models

import com.google.inject.Inject
import helpers.OIDCHelper
import play.api.mvc._
import play.api.mvc.Results._
import services.OIDCAuthService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cobbled this together from:
  * https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Authentication
  */
class AuthenticatedUserAction @Inject()(val parser: BodyParsers.Default, authprovider: OIDCAuthService)(
  implicit val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent] {
  private val logger = play.api.Logger(this.getClass)
  implicit val scheduler = monix.execution.Scheduler.Implicits.global

  /**
    * This action helps us identify a user. If the user is not logged in a 401 is returned
    * @param request The current request
    * @param block The play action the user wants to perform
    * @tparam A  The type of the request (AnyContent)
    * @return The play action with the user info or Unauthorized
    */
  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    authprovider.getUserInfo(request.headers).runToFuture.flatMap { user =>
      val token = OIDCHelper.getTokenFromRequest[A](request)
      if (user.isDefined) {
        block(new UserRequest(user.get, request, token))
      } else {
        Future.successful(Unauthorized("You must be logged in to execute this request"))
      }
    }
  }

}
