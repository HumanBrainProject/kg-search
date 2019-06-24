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
import models.errors.APIEditorError
import models.instance.NexusInstanceReference
import models.{AccessToken, NexusPath}
import monix.eval.Task
import play.api.Logger
import services.EditorService

final case class DeleteLinkingInstanceCommand(
  from: NexusInstanceReference,
  to: NexusInstanceReference,
  linkingInstancePath: NexusPath,
  editorService: EditorService,
  token: AccessToken
) extends Command {
  private val log = Logger(this.getClass)
  override def execute(): Task[Either[APIEditorError, Unit]] = {
    editorService
      .deleteLinkingInstance(
        from,
        to,
        linkingInstancePath,
        token
      )
      .map {
        case Left(errors) =>
          log.error(s"Could not delete all linking instances ${errors.content.mkString("\n")}")
          Left(errors.content.head)
        case Right(()) => Right(())
      }
  }
}
