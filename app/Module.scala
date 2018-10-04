import com.google.inject.AbstractModule
import repositories.FootballMatchRepository

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[FootballMatchRepository]).asEagerSingleton()
  }
}
