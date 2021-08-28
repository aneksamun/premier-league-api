import com.google.inject.AbstractModule
import repositories.{FootballMatchMongoRepository, FootballMatchRepository}
import services.{FootballMatchService, TableService}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[FootballMatchRepository])
      .to(classOf[FootballMatchMongoRepository])
      .asEagerSingleton()

    bind(classOf[FootballMatchService]).asEagerSingleton()

    bind(classOf[TableService]).asEagerSingleton()
  }
}
