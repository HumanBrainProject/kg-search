package services.indexer

import javax.inject.Inject
import monix.eval.Task
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import utils.Template

trait Indexer[Content, TransformedContent, Effect[_]] {

  def transform(jsonContent: Content, template: Map[String, Template]): TransformedContent
  def load(jsValue: TransformedContent): Effect[WSResponse]

}

class IndexerImpl @Inject()() extends Indexer[JsValue, JsValue, Task] {

  override def load(jsValue: JsValue): Task[WSResponse] = ???

  override def transform(jsonContent: JsValue, template: Map[String, Template]): JsValue = {
    TemplateEngine.transform(jsonContent, template)
  }
}
