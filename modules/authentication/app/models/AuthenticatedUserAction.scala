
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

package models.authentication

import com.google.inject.Inject
import play.api.mvc._
import play.api.mvc.Results._
import service.authentication.OIDCAuthService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cobbled this together from:
  * https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Authentication
  */
class AuthenticatedUserAction @Inject()(val parser: BodyParsers.Default, authprovider: OIDCAuthService)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {
  private val logger = play.api.Logger(this.getClass)
  override def invokeBlock[A](request: Request[A],
                              block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    authprovider.getUserInfo(request.headers).flatMap{ user =>
      if(user.isDefined){
        block(new UserRequest(user.get, request))
      }else{
        Future.successful(Unauthorized("You must be logged in to execute this request"))
      }
    }
  }

}