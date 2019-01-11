package services

import com.google.inject.Inject
import models.errors.APIEditorError
import models.instance.{EditorMetadata, NexusInstanceReference}
import play.api.libs.ws.WSClient

import scala.concurrent.Future

class MetadataService @Inject()(WSClient: WSClient) {

  def getMetadata(nexusInstanceReference: NexusInstanceReference): Future[Either[APIEditorError, EditorMetadata]] = ???
}
