package editor.controllers

import authentication.models.{AuthenticatedUserAction, UserRequest}
import authentication.service.OIDCAuthService
import com.google.inject.Inject
import common.models.OIDCUser
import play.api.mvc.{BodyParsers, Request, Result}

import scala.concurrent.{ExecutionContext, Future}


class TestAuthenticatedUserAction @Inject()(
                                             override val parser: BodyParsers.Default,
                                             authprovider: OIDCAuthService,
                                             userInfo: OIDCUser
                                           )(implicit override val executionContext: ExecutionContext)
  extends AuthenticatedUserAction(parser, authprovider)(executionContext){
  override def invokeBlock[A](request: Request[A],
                              block: (UserRequest[A]) => Future[Result]): Future[Result] = {
      block(new UserRequest(userInfo, request))
  }

}
