package integration

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class PremierLeagueTable extends PlaySpec with GuiceOneAppPerSuite
  with DockerTestKit with DockerKitSpotify
    with MongoDbContainer {

  // https://github.com/ReactiveMongo/Play-ReactiveMongo/blob/master/src/test/scala/PlaySpec.scala

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .load(
        new play.api.i18n.I18nModule,
        new play.api.mvc.CookiesModule,
        new play.api.inject.BuiltinModule,
        new play.modules.reactivemongo.ReactiveMongoModule
      )
      .configure(Map("mongodb.uri" -> s"mongodb://localhost:$MongoDbPort/football-db"))
      .build()

  "The Premier League API " must {
    "retrieve game results for given week" in {
      val week = 1
      val client = app.injector.instanceOf[WSClient]
      val callbackUrl = s"http://localhost:$testServerPort/games/$week"

      val response = await(client.url(callbackUrl).get())

      response.status mustBe OK
    }
  }
}

