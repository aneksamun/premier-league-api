package integration

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.await
import play.api.test.Helpers._

class TableIT extends BaseIT {
  import Endpoints._

  "The user " must {
    "view Premier League table ordered by position and then goal difference" in {
      val games = Vector(
        Json.obj(fields =
          "gameWeek" -> 1,
          "homeTeam" -> "Arsenal",
          "awayTeam" -> "Leicester",
          "homeGoals" -> 2,
          "awayGoals" -> 1
        ),
        Json.obj(fields =
          "gameWeek" -> 1,
          "homeTeam" -> "Manchester United",
          "awayTeam" -> "Leicester",
          "homeGoals" -> 0,
          "awayGoals" -> 1
        ),
        Json.obj(fields =
          "gameWeek" -> 2,
          "homeTeam" -> "Arsenal",
          "awayTeam" -> "Sunderland",
          "homeGoals" -> 1,
          "awayGoals" -> 1
        ),
        Json.obj(fields =
          "gameWeek" -> 2,
          "homeTeam" -> "Sunderland",
          "awayTeam" -> "Manchester United",
          "homeGoals" -> 1,
          "awayGoals" -> 1
        ),
        Json.obj(fields =
          "gameWeek" -> 3,
          "homeTeam" -> "Chelsea",
          "awayTeam" -> "Manchester United",
          "homeGoals" -> 3,
          "awayGoals" -> 2
        )
      )
      val offset = 1
      val limit = 10

      games foreach { game => await(client.url(AddGameUrl) post game) }

      val response = await(client.url(GetTableUrl).addQueryStringParameters(
        ("offset", offset.toString),
        ("limit", limit.toString)
      ).get())

      response.status mustBe OK

      (response.json \ "offset").as[Int] mustBe offset
      (response.json \ "limit").as[Int] mustBe limit
      (response.json \ "total").as[Int] mustBe 5

      val firstPos = (response.json \ "items")(0).asInstanceOf[JsObject]
      val secondPos = (response.json \ "items")(1).asInstanceOf[JsObject]
      val thirdPos = (response.json \ "items")(2).asInstanceOf[JsObject]
      val fourthPos = (response.json \ "items")(3).asInstanceOf[JsObject]
      val fifthPos = (response.json \ "items")(4).asInstanceOf[JsObject]

      firstPos("position").as[Int] mustBe 1
      firstPos("team").as[String] mustBe "Arsenal"
      firstPos("gamesPlayed").as[Int] mustBe 2
      firstPos("gamesWon").as[Int] mustBe 1
      firstPos("gamesDrawn").as[Int] mustBe 1
      firstPos("gamesLost").as[Int] mustBe 0
      firstPos("goalsFor").as[Int] mustBe 3
      firstPos("goalsAgainst").as[Int] mustBe 2
      firstPos("goalDifference").as[Int] mustBe 1
      firstPos("points").as[Int] mustBe 4

      secondPos("position").as[Int] mustBe 2
      secondPos("team").as[String] mustBe "Chelsea"
      secondPos("gamesPlayed").as[Int] mustBe 1
      secondPos("gamesWon").as[Int] mustBe 1
      secondPos("gamesDrawn").as[Int] mustBe 0
      secondPos("gamesLost").as[Int] mustBe 0
      secondPos("goalsFor").as[Int] mustBe 3
      secondPos("goalsAgainst").as[Int] mustBe 2
      secondPos("goalDifference").as[Int] mustBe 1
      secondPos("points").as[Int] mustBe 3

      thirdPos("position").as[Int] mustBe 3
      thirdPos("team").as[String] mustBe "Leicester"
      thirdPos("gamesPlayed").as[Int] mustBe 2
      thirdPos("gamesWon").as[Int] mustBe 1
      thirdPos("gamesDrawn").as[Int] mustBe 0
      thirdPos("gamesLost").as[Int] mustBe 1
      thirdPos("goalsFor").as[Int] mustBe 2
      thirdPos("goalsAgainst").as[Int] mustBe 2
      thirdPos("goalDifference").as[Int] mustBe 0
      thirdPos("points").as[Int] mustBe 3

      fourthPos("position").as[Int] mustBe 4
      fourthPos("team").as[String] mustBe "Sunderland"
      fourthPos("gamesPlayed").as[Int] mustBe 2
      fourthPos("gamesWon").as[Int] mustBe 0
      fourthPos("gamesDrawn").as[Int] mustBe 2
      fourthPos("gamesLost").as[Int] mustBe 0
      fourthPos("goalsFor").as[Int] mustBe 2
      fourthPos("goalsAgainst").as[Int] mustBe 2
      fourthPos("goalDifference").as[Int] mustBe 0
      fourthPos("points").as[Int] mustBe 2

      fifthPos("position").as[Int] mustBe 5
      fifthPos("team").as[String] mustBe "Manchester United"
      fifthPos("gamesPlayed").as[Int] mustBe 3
      fifthPos("gamesWon").as[Int] mustBe 0
      fifthPos("gamesDrawn").as[Int] mustBe 1
      fifthPos("gamesLost").as[Int] mustBe 2
      fifthPos("goalsFor").as[Int] mustBe 3
      fifthPos("goalsAgainst").as[Int] mustBe 5
      fifthPos("goalDifference").as[Int] mustBe -2
      fifthPos("points").as[Int] mustBe 1
    }
  }
}
