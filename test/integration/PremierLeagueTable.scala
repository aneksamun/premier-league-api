package integration

import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class PremierLeagueTable extends PlaySpec with GuiceOneAppPerSuite
  with DockerTestKit with DockerKitSpotify
    with MongoDbContainer {

  "The Premier League API " must {
    "retrieve game results for given week" in {
      val week = 1
      val client = app.injector.instanceOf[WSClient]
      val callbackUrl = "http://localhost:$port/games/$week"

      val response = await(client.url(callbackUrl).get())

      response.status mustBe OK
    }
  }
}
