package editor.actions
import authentication.models.UserRequest
import play.api.mvc._
import play.api.mvc.Results._
import com.google.inject.Inject
import editor.helpers.EditorSpaceHelper
import editor.models.EditorUserRequest

import scala.concurrent.{ExecutionContext, Future}


object EditorUserAction{

  def editorUserAction()(implicit ec: ExecutionContext) = new ActionRefiner[UserRequest, EditorUserRequest] {
    def executionContext = ec
    def refine[A](input: UserRequest[A]) = Future.successful {
      input.headers.get("index-hint") match {
        case Some(index) =>
          if(EditorSpaceHelper.isEditorGroup(input.user, index) ){
            Right(new EditorUserRequest(input.user, index, input))
          }else{
            Left(Forbidden("You are not allowed to perform this request"))
          }
        case _ => Left(Forbidden("You are not allowed to perform this request"))
      }
    }
  }
}