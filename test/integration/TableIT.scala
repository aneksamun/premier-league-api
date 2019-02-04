package integration

import play.api.libs.json.Json
import play.api.test.Helpers.await
import play.api.test.Helpers._

class TableIT extends BaseIT {
  import Endpoints._

  "The user " must {
    "get Premier League table" in {
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
          "awayTeam" -> "Manchester United.",
          "homeGoals" -> 3,
          "awayGoals" -> 2
        )
      )

      games foreach { game => await(client.url(AddGameUrl) post game) }

      val response = await(client.url(GetTableUrl).get())

      response.status mustBe OK

      """
      {"offset":1,"limit":20,"total":6,"items":[
        {"position":1,"team":"Arsenal","gamesPlayed":2,"gamesWon":1,"gamesDrawn":1,"gamesLost":0,"goalsFor":3,"goalsAgainst":2,"goalDifference":1,"points":4},
        {"position":2,"team":"Chelsea","gamesPlayed":1,"gamesWon":1,"gamesDrawn":0,"gamesLost":0,"goalsFor":3,"goalsAgainst":2,"goalDifference":1,"points":3},
        {"position":3,"team":"Leicester","gamesPlayed":2,"gamesWon":1,"gamesDrawn":0,"gamesLost":1,"goalsFor":2,"goalsAgainst":2,"goalDifference":0,"points":3},
        {"position":4,"team":"Sunderland","gamesPlayed":2,"gamesWon":0,"gamesDrawn":2,"gamesLost":0,"goalsFor":2,"goalsAgainst":2,"goalDifference":0,"points":2},
        {"position":5,"team":"Manchester United","gamesPlayed":2,"gamesWon":0,"gamesDrawn":1,"gamesLost":1,"goalsFor":1,"goalsAgainst":2,"goalDifference":-1,"points":1},
        {"position":6,"team":"Manchester United.","gamesPlayed":1,"gamesWon":0,"gamesDrawn":0,"gamesLost":1,"goalsFor":2,"goalsAgainst":3,"goalDifference":-1,"points":0}]
      }"""
    }
  }
}
