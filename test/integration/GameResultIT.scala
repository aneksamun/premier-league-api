package integration

import com.fasterxml.jackson.annotation.JsonValue
import com.google.gson.JsonArray
import play.api.libs.json.Json
import play.api.test.Helpers._

class GameResultIT extends BaseIT {
  import Endpoints._

  "The user " must {
    "add new games" in {
      val game = Json.obj(
        "gameWeek" -> 1,
        "homeTeam" -> "Tottenham Hotspur F.C.",
        "awayTeam" -> "Arsenal F.C.",
        "homeGoals" -> 3,
        "awayGoals" -> 1
      )
      await(client.url(AddGameUrl) post game).status mustBe CREATED
    }
  }

  it must {
    "retrieve game results for given week" in {
      val week = 1
      val requests = Vector(
        Json.obj(fields =
          "gameWeek" -> week,
          "homeTeam" -> "Chelsea",
          "awayTeam" -> "Manchester United",
          "homeGoals" -> 3,
          "awayGoals" -> 2
        ),
        Json.obj(fields =
          "gameWeek" -> week,
          "homeTeam" -> "Arsenal",
          "awayTeam" -> "Leicester",
          "homeGoals" -> 2,
          "awayGoals" -> 1
        )
      )

      requests takeWhile { request => await(client.url(AddGameUrl) post request).status equals CREATED }

      val callbackUrl: String = GetGamesUrl.withArgs(Array(week))
      val response = await(client.url(callbackUrl).get())

      response.status mustBe OK

      response.json(0)
      response.json(1)
    }
  }
}

