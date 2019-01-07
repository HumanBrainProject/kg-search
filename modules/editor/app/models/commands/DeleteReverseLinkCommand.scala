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
package models.commands
import helpers.ReverseLinkOP
import models.errors.APIEditorError
import models.instance.{EditorInstance, NexusInstance, NexusInstanceReference, NexusLink}
import models.user.User
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import services.EditorService

import scala.concurrent.{ExecutionContext, Future}

case class DeleteReverseLinkCommand(
  reverseInstanceLink: NexusLink,
  reverseInstance: NexusInstance,
  targetField: String,
  currentInstanceRef: NexusInstanceReference,
  editorService: EditorService,
  baseUrl: String,
  token: String,
  user: User
) extends Command {
  override def execute()(implicit executionContext: ExecutionContext): Future[Either[APIEditorError, Unit]] = {
    val diffToSave = reverseInstance.content.value.get(targetField) match {
      case Some(fieldValue) => ReverseLinkOP.removeLink(fieldValue, currentInstanceRef, targetField)
      case None             =>
        //The field does not exists we do nothing
        Right(None)
    }
    diffToSave match {
      case Right(Some(l)) =>
        val reverseLinkInstance = EditorInstance(
          NexusInstance(
            Some(reverseInstanceLink.ref.id),
            reverseInstanceLink.ref.nexusPath,
            Json.obj(targetField -> Json.toJson(l.map(_.toJson(baseUrl))))
          )
        )
        editorService.updateInstance(reverseLinkInstance, reverseInstanceLink.ref, token, user.id)
      case Right(None) => Future(Right(()))
      case Left(s)     => Future(Left(APIEditorError(INTERNAL_SERVER_ERROR, s)))
    }
  }
}
