package editor
import com.google.inject.AbstractModule
import services.specification.FormService

class EditorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[FormService]).asEagerSingleton()
  }
}
