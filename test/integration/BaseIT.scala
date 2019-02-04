package integration

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import scala.language.implicitConversions

abstract class BaseIT extends PlaySpec with GuiceOneServerPerSuite
  with DockerTestKit with DockerKitSpotify
    with MongoDbContainer {

  private val DatabaseName = "football-db"

  protected val client: WSClient = app.injector.instanceOf[WSClient]

  sealed trait CallbackUrl {
    val value: String
    def withArgs(args: Array[Any]): String = value.format(args:_*)
  }

  object Endpoints {
    case object AddGameUrl extends CallbackUrl {
      override val value: String = s"http://localhost:$port/games"
    }
    case object GetGamesUrl extends CallbackUrl {
      override val value: String = s"http://localhost:$port/games/%d"
    }
    case object GetTableUrl extends CallbackUrl {
      override val value: String = s"http://localhost:$port/table"
    }

    implicit def callback2String(url: CallbackUrl): String = url.value
  }

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map(
      "play.http.filters" -> "play.api.http.NoHttpFilters",
      "mongodb.uri" -> s"mongodb://$MongoDbHostName:$MongoDbPort/$DatabaseName"
    ))
    .build()
}
