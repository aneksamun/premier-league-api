package integration

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class PremierLeagueTable extends PlaySpec with GuiceOneServerPerSuite
  with DockerTestKit with DockerKitSpotify
    with MongoDbContainer {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map(
      "play.http.filters" -> "play.api.http.NoHttpFilters",
      "mongodb.uri" -> mongoUri()
    ))
    .build()

  "The Premier League API " must {
    "retrieve game results for given week" in {
      containerManager.isReady(mongodbContainer).isCompleted mustBe true

      val week = 1
      val client = app.injector.instanceOf[WSClient]
      val callbackUrl = s"http://localhost:$port/games/$week"

      val response = await(client.url(callbackUrl).get())

      response.status mustBe OK
    }
  }
}
