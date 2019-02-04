package controllers

import models.{FootballMatch, GameResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Lang
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import services.FootballMatchService

import scala.concurrent.Future

class GameControllerSpec extends PlaySpec with Results with MockitoSugar with TableDrivenPropertyChecks {

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

    when(footballMatchService add any[FootballMatch]) thenReturn Future.successful(
      UpdateWriteResult(ok = true, 1, 1, Nil, Nil, None, None, None)
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
    val english = Lang("en")

    val messages = Map(
      "error.required" -> "Field is required",
      "error.min" -> "Minimum allowed value is {0}",
      "error.max" -> "Maximum allowed value is {0}"
    )

    val gameController = new GameController(
      stubControllerComponents(),
      footballMatchService,
      stubMessagesApi(Map(english.code -> messages)),
      stubLangs(Seq(english))
    )

    val expectations = Table(
      "expectations", (
        Json.obj(fields =
          "gameWeek" -> 0,
          "homeTeam" -> "",
          "awayTeam" -> "",
          "homeGoals" -> -1,
          "awayGoals" -> -1
        ),
        Json.arr(
          Json.obj(fields =
            "field" -> "gameWeek",
            "error" -> "Minimum allowed value is 1"
          ),
          Json.obj(fields =
            "field" -> "homeTeam",
            "error" -> "Field is required"
          ),
          Json.obj(fields =
            "field" -> "awayTeam",
            "error" -> "Field is required"
          ),
          Json.obj(fields =
            "field" -> "homeGoals",
            "error" -> "Minimum allowed value is 0"
          ),
          Json.obj(fields =
            "field" -> "awayGoals",
            "error" -> "Minimum allowed value is 0"
          )
        )
      ), (
        Json.obj(fields =
          "gameWeek" -> 39,
          "homeTeam" -> JsNull,
          "awayTeam" -> JsNull,
          "homeGoals" -> -10,
          "awayGoals" -> -2
        ),
        Json.arr(
          Json.obj(fields =
            "field" -> "gameWeek",
            "error" -> "Maximum allowed value is 38"
          ),
          Json.obj(fields =
            "field" -> "homeTeam",
            "error" -> "Field is required"
          ),
          Json.obj(fields =
            "field" -> "awayTeam",
            "error" -> "Field is required"
          ),
          Json.obj(fields =
            "field" -> "homeGoals",
            "error" -> "Minimum allowed value is 0"
          ),
          Json.obj(fields =
            "field" -> "awayGoals",
            "error" -> "Minimum allowed value is 0"
          )
        )
      )
    )

    forAll(expectations) { expectation =>
      val request = expectation._1
      val errors = expectation._2

      val result = gameController.add().apply(FakeRequest().withJsonBody(request))
      status(result) mustBe BAD_REQUEST

      val actualErrors = contentAsJson(result)

      val actualWeekError = actualErrors(0)
      val actualHomeTeamError = actualErrors(1)
      val actualAwayTeamError = actualErrors(2)
      val actualHomeGoalsError = actualErrors(3)
      val actualAwayGoalsError = actualErrors(4)

      val expectedWeekError = errors(0)
      val expectedHomeTeamError = errors(1)
      val expectedAwayTeamError = errors(2)
      val expectedHomeGoalsError = errors(3)
      val expectedAwayGoalsError = errors(4)

      actualWeekError("field").as[String] mustBe expectedWeekError("field").as[String]
      actualWeekError("error").as[String] mustBe expectedWeekError("error").as[String]

      actualHomeTeamError("field").as[String] mustBe expectedHomeTeamError("field").as[String]
      actualHomeTeamError("error").as[String] mustBe expectedHomeTeamError("error").as[String]

      actualAwayTeamError("field").as[String] mustBe expectedAwayTeamError("field").as[String]
      actualAwayTeamError("error").as[String] mustBe expectedAwayTeamError("error").as[String]

      actualHomeGoalsError("field").as[String] mustBe expectedHomeGoalsError("field").as[String]
      actualHomeGoalsError("error").as[String] mustBe expectedHomeGoalsError("error").as[String]

      actualAwayGoalsError("field").as[String] mustBe expectedAwayGoalsError("field").as[String]
      actualAwayGoalsError("error").as[String] mustBe expectedAwayGoalsError("error").as[String]
    }
  }
}
