package controllers

import models.GameResult
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FootballMatchService

import scala.concurrent.Future

class GameControllerSpec extends PlaySpec with Results with MockitoSugar {

  "GameController" must {
    "retrieve game results for concrete week" in {
      val week = 3
      val footballMatchService = mock[FootballMatchService]

      val gameController = new GameController(
        stubControllerComponents(),
        footballMatchService,
        stubMessagesApi(),
        stubLangs()
      )

      val results = Seq(GameResult(
        homeTeam = "homies",
        awayTeam = "aliens",
        homeGoals = 1,
        awayGoals = 1,
        result = "Draw"
      ))

      when(footballMatchService getResults week) thenReturn Future.successful(results)

      val result = gameController.index(week).apply(FakeRequest())

      status(result) mustBe OK

      val gameResult = contentAsJson(result).head

      (gameResult \ "homeTeam").as[String] mustBe results.head.homeTeam
      (gameResult \ "awayTeam").as[String] mustBe results.head.awayTeam
      (gameResult \ "homeGoals").as[Int] mustBe results.head.homeGoals
      (gameResult \ "awayGoals").as[Int] mustBe results.head.awayGoals
      (gameResult \ "result").as[String] mustBe results.head.result
    }
  }

  "insert a valid football match" in {
    val footballMatchService = mock[FootballMatchService]

    val gameController = new GameController(
      stubControllerComponents(),
      footballMatchService,
      stubMessagesApi(),
      stubLangs()
    )

    status(
      gameController.add().apply(FakeRequest().withJsonBody(
        Json.obj(fields =
          "gameWeek" -> 1,
          "homeTeam" -> "Homies",
          "awayTeam" -> "Aliens",
          "homeGoals" -> 1,
          "awayGoals" -> 1
        )
      ))
    ) mustBe CREATED
  }

  "fail to insert invalid football match record" in {
    val footballMatchService = mock[FootballMatchService]

    val gameController = new GameController(
      stubControllerComponents(),
      footballMatchService,
      stubMessagesApi(),
      stubLangs(Seq(Lang("en")))
    )

    status(
      gameController.add().apply(FakeRequest().withJsonBody(
        Json.obj(fields =
          "gameWeek" -> 0,
          "homeTeam" -> "Homies",
          "awayTeam" -> "Aliens",
          "homeGoals" -> 1,
          "awayGoals" -> 1
        )
      ))
    ) mustBe BAD_REQUEST
  }
}
