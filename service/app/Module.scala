import com.google.inject.AbstractModule
import monix.eval.Task
import play.api.libs.json.JsValue
import services.indexer.{Indexer, IndexerImpl}

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[Indexer[JsValue, JsValue, Task]])
      .to(classOf[IndexerImpl])

  }
}
