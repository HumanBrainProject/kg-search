package editor
import com.google.inject.AbstractModule
import services.specification.SpecificationService

class EditorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SpecificationService]).asEagerSingleton()
  }
}
