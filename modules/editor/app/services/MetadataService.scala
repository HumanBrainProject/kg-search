package services

import com.google.inject.Inject
import models.errors.APIEditorError
import models.instance.{EditorMetadata, NexusInstanceReference}
import org.joda.time.DateTime
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class MetadataService @Inject()(WSClient: WSClient)(implicit executionContext: ExecutionContext) {

  def getMetadata(nexusInstanceReference: NexusInstanceReference): Future[Either[APIEditorError, EditorMetadata]] = {
    Future(
      Right(
        EditorMetadata(
          Some(new DateTime()),
          None,
          "Roger F.",
          25
        )
      )
    )
  }
}
