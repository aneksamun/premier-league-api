package services

import models.{FootballMatch, GameResult}
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import repositories.FootballMatchRepository

import scala.concurrent.Future

class FootballMatchServiceSpec extends FlatSpec with MockitoSugar with Matchers {

  "FootballMatchService#add" should "call repository" in {
    val footballMatchRepository = mock[FootballMatchRepository]
    val footballMatchService = new FootballMatchService(footballMatchRepository)

    val footballMatch = FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 0,
      awayGoals = 0
    )

    footballMatchService add footballMatch

    verify(footballMatchRepository, times(1)) add footballMatch
  }

  "FootballMatchService#getResults" should "get results from obtained football matches" in {
    val givenWeek = 1

    val footballMatch = FootballMatch(
      gameWeek = 1,
      homeTeam = "TeamA",
      awayTeam = "TeamB",
      homeGoals = 19,
      awayGoals = 10
    )

    val footballMatchRepository = mock[FootballMatchRepository]
    val footballMatchService = new FootballMatchService(footballMatchRepository)

    when(footballMatchRepository findForWeek givenWeek) thenReturn Future.successful(Seq(footballMatch))

    val results = await(footballMatchService getResults givenWeek)

    results should equal (Seq(
      GameResult(
        homeTeam = "TeamA",
        awayTeam = "TeamB",
        homeGoals = 19,
        awayGoals = 10,
        result = "Home Win"
      )
    ))
  }
}
