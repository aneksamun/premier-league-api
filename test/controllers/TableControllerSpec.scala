package controllers

import binders.PagingParams
import models.{Page, TeamStanding}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TableService

import scala.concurrent.Future

class TableControllerSpec extends PlaySpec with Results with MockitoSugar {

  "Table controller" must {
    "retrieve team standings" in {
      val tableService = mock[TableService]
      val gameController = new TableController(stubControllerComponents(), tableService)
      val params = PagingParams(1, 20)

      val standings = Seq(
        TeamStanding(
          position = 1,
          team = "champions",
          gamesPlayed = 8,
          gamesDrawn = 2,
          gamesWon = 6,
          gamesLost = 0,
          goalsAgainst = 2,
          goalsFor = 10,
          goalDifference = 8,
          points = 20
        )
      )

      when(tableService getTable(params.offset, params.limit)) thenReturn Future.successful(
        Page[TeamStanding](
          offset = params.offset,
          limit = params.limit,
          total = standings.size,
          items = standings
        )
      )

      val result = gameController.index(params).apply(FakeRequest())

      status(result) mustBe OK

      val page = contentAsJson(result)

      (page \ "offset").as[Int] mustBe params.offset
      (page \ "limit").as[Int] mustBe params.limit
      (page \ "total").as[Int] mustBe standings.size

      val item = (page \ "items").head

      (item get "position").as[Int] mustBe standings.head.position
      (item get "team").as[String] mustBe standings.head.team
      (item get "gamesPlayed").as[Int] mustBe standings.head.gamesPlayed
      (item get "gamesWon").as[Int] mustBe standings.head.gamesWon
      (item get "gamesDrawn").as[Int] mustBe standings.head.gamesDrawn
      (item get "gamesPlayed").as[Int] mustBe standings.head.gamesPlayed
      (item get "goalsFor").as[Int] mustBe standings.head.goalsFor
      (item get "goalsAgainst").as[Int] mustBe standings.head.goalsAgainst
      (item get "goalDifference").as[Int] mustBe standings.head.goalDifference
      (item get "points").as[Int] mustBe standings.head.points
    }
  }
}
