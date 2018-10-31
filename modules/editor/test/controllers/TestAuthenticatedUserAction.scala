package editor.controllers

import com.google.inject.Inject
import models.{AuthenticatedUserAction, UserRequest}
import models.user.NexusUser
import play.api.mvc.{BodyParsers, Request, Result}
import services.OIDCAuthService

import scala.concurrent.{ExecutionContext, Future}


class TestAuthenticatedUserAction @Inject()(
                                             override val parser: BodyParsers.Default,
                                             authprovider: OIDCAuthService,
                                             userInfo: NexusUser
                                           )(implicit override val executionContext: ExecutionContext)
  extends AuthenticatedUserAction(parser, authprovider)(executionContext){
  override def invokeBlock[A](request: Request[A],
                              block: (UserRequest[A]) => Future[Result]): Future[Result] = {
      block(new UserRequest(userInfo, request))
  }

}
