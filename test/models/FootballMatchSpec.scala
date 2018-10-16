package models

import org.scalatest.{FlatSpec, Matchers}

class FootballMatchSpec extends FlatSpec
  with Matchers {

  "Football match between teams with the same goals count" should "be draw" in {
    FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 3,
      awayGoals = 3
    ).summary shouldBe "Draw"
  }

  "Home team with more goals " should "win" in {
    FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 4,
      awayGoals = 3
    ).summary shouldBe "Home Win"
  }

  "Away team with more goals" should "win" in {
    FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 3,
      awayGoals = 4
    ).summary shouldBe "Away Win"
  }

  "Game result " should "be built from football match" in {
    val expected = GameResult(
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 1,
      awayGoals = 1,
      result = "Draw"
    )

    val actual = FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 1,
      awayGoals = 1
    ).result

    actual should equal (expected)
  }
}
