package editor.models

import authentication.models.UserInfo
import play.api.mvc.{Request, WrappedRequest}

/**
  *
  * @param user
  * @param editorGroup
  * @param request
  * @tparam A
  */
class EditorUserRequest[A](val user: UserInfo, editorGroup: String, request: Request[A]) extends WrappedRequest[A](request)