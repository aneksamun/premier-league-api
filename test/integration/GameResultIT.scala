package integration

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
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
    "retrieve game results for given week in sorted order by home team" in {
      val week = 1
      val requests = Vector(
        Json.obj(fields =
          "gameWeek" -> week,
          "homeTeam" -> "Chelsea",
          "awayTeam" -> "Manchester United",
          "homeGoals" -> 3,
          "awayGoals" -> 4
        ),
        Json.obj(fields =
          "gameWeek" -> week,
          "homeTeam" -> "Arsenal",
          "awayTeam" -> "Leicester",
          "homeGoals" -> 2,
          "awayGoals" -> 1
        )
      )

      requests foreach { request => await(client.url(AddGameUrl) post request) }

      val callbackUrl: String = GetGamesUrl.withArgs(Array(week))
      val response: WSResponse = await(client.url(callbackUrl).get())

      response.status mustBe OK

      (response.json(0) \ "homeTeam").as[String] mustBe (requests(1) \ "homeTeam").as[String]
      (response.json(0) \ "awayTeam").as[String] mustBe (requests(1) \ "awayTeam").as[String]
      (response.json(0) \ "homeGoals").as[Int] mustBe (requests(1) \ "homeGoals").as[Int]
      (response.json(0) \ "awayGoals").as[Int] mustBe (requests(1) \ "awayGoals").as[Int]
      (response.json(0) \ "result").as[String] mustBe "Home Win"

      (response.json(1) \ "homeTeam").as[String] mustBe (requests(0) \ "homeTeam").as[String]
      (response.json(1) \ "awayTeam").as[String] mustBe (requests(0) \ "awayTeam").as[String]
      (response.json(1) \ "homeGoals").as[Int] mustBe (requests(0) \ "homeGoals").as[Int]
      (response.json(1) \ "awayGoals").as[Int] mustBe (requests(0) \ "awayGoals").as[Int]
      (response.json(1) \ "result").as[String] mustBe "Away Win"
    }
  }
}
