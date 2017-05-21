import com.google.inject.AbstractModule
import service.ImportSchedule

class AppModule extends AbstractModule {

  override def configure() = {
    bind(classOf[ImportSchedule]).asEagerSingleton()
  }

}
