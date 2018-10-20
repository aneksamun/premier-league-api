package services

import models.{Page, TeamStanding}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.Configuration
import play.api.test.Helpers._
import repositories.FootballMatchRepository

import scala.concurrent.Future

class TableServiceSpec extends FlatSpec with MockitoSugar with Matchers {

  "TableServer#getTable" should "retrieve a table" in {
    val offset = 1
    val limit = 20
    val drawPoints = 1
    val victoryPoints = 3

    val standings = Seq(TeamStanding(
      position = 1,
      team = "Winners",
      gamesPlayed = 3,
      gamesWon = 3,
      gamesDrawn = 0,
      gamesLost = 0,
      goalsFor = 9,
      goalsAgainst = 3,
      goalDifference = 6,
      points = 9
    ))

    val configuration = mock[Configuration]
    val footballMatchRepository = mock[FootballMatchRepository]
    lazy val tableService = new TableService(footballMatchRepository, configuration)

    when(configuration get[Int] "draw.points") thenReturn drawPoints
    when(configuration get[Int] "victory.points") thenReturn victoryPoints

    when(footballMatchRepository countTeams) thenReturn Future.successful(standings.size)

    when(footballMatchRepository getTeamStandings (victoryPoints, drawPoints, offset, limit)) thenReturn
      Future.successful(standings)

    val page = await(tableService getTable (offset, limit))

    page should equal (Page(
      offset = offset,
      limit = limit,
      total = standings.size,
      items = standings
    ))
  }
}
