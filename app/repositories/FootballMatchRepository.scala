package repositories

import javax.inject.{Inject, Singleton}
import models.{FootballMatch, Page, TeamStanding}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FootballMatchRepository @Inject()(implicit ec: ExecutionContext, api: ReactiveMongoApi) {

  import models.JsonFormats._

  val footballMatches: Future[JSONCollection] = api.database.map(_.collection(name = "football_matches"))

  footballMatches.map(_.indexesManager.ensure(Index(List(
    "homeTeam" -> Ascending,
    "awayTeam" -> Ascending,
    "gameWeek" -> Ascending), unique = true)
  ))

  def add(footballMatch: FootballMatch): Future[WriteResult] = {
    footballMatches.flatMap(_.insert(footballMatch))
  }

  def findForWeek(week: Int): Future[Seq[FootballMatch]] = {
    footballMatches.flatMap(_.find(Json.obj("gameWeek" -> week), Option(Json.obj()))
      .sort(Json.obj("homeTeam" -> 1))
      .cursor[FootballMatch](ReadPreference.primary)
      .collect[Seq](10000, Cursor.FailOnError[Seq[FootballMatch]]())
    )
  }

  def getTable(offset: Int, limit: Int, victoryPoint: Int, drawPoint: Int): Future[Page[TeamStanding]] = {
    footballMatches.flatMap(collection => {
      import collection.BatchCommands.AggregationFramework._

      collection.aggregatorContext[Page[TeamStanding]](
        PipelineOperator(Json.obj("$facet" -> Json.obj(
          "homeTeamStandings" -> Seq(
            Group(JsString("$homeTeam"))(
              "gamesPlayed" -> SumAll,
              "goalsFor" -> SumField("homeGoals"),
              "goalsAgainst" -> SumField("awayGoals"),
              "gamesWon" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$gt" -> Seq("$homeGoals", "$awayGoals")), "then" -> 1, "else" -> 0))),
              "gamesDrawn" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$eq" -> Seq("$homeGoals", "$awayGoals")), "then" -> 1, "else" -> 0))),
              "gamesLost" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$lt" -> Seq("$homeGoals", "$awayGoals")), "then" -> 1, "else" -> 0)))
            ).makePipe,
            Project(Json.obj(fields =
              "team" -> JsString("$_id"),
              "gamesPlayed" -> JsString("$gamesPlayed"),
              "gamesWon" -> JsString("$gamesWon"),
              "gamesDrawn" -> JsString("$gamesDrawn"),
              "gamesLost" -> JsString("$gamesLost"),
              "goalsFor" -> JsString("$goalsFor"),
              "goalsAgainst" -> JsString("$goalsAgainst"),
              "goalDifference" -> Json.obj("$subtract" -> Seq("$goalsFor", "$goalsAgainst")),
              "points" -> Json.obj("$add" -> Seq(
                Json.obj("$multiply" -> Seq(JsString("$gamesWon"), JsNumber(victoryPoint))),
                Json.obj("$multiply" -> Seq(JsString("$gamesDrawn"), JsNumber(drawPoint)))
              ))
            )).makePipe
          ),
          "awayTeamStandings" -> Seq(
            Group(JsString("$awayTeam"))(
              "gamesPlayed" -> SumAll,
              "goalsFor" -> SumField("awayGoals"),
              "goalsAgainst" -> SumField("homeGoals"),
              "gamesWon" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$gt" -> Seq("$awayGoals", "$homeGoals")), "then" -> 1, "else" -> 0))),
              "gamesDrawn" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$eq" -> Seq("$awayGoals", "$homeGoals")), "then" -> 1, "else" -> 0))),
              "gamesLost" -> Sum(Json.obj("$cond" -> Json.obj("if" -> Json.obj("$lt" -> Seq("$awayGoals", "$homeGoals")), "then" -> 1, "else" -> 0)))
            ).makePipe,
            Project(Json.obj(fields =
              "team" -> JsString("$_id"),
              "gamesPlayed" -> JsString("$gamesPlayed"),
              "gamesWon" -> JsString("$gamesWon"),
              "gamesDrawn" -> JsString("$gamesDrawn"),
              "gamesLost" -> JsString("$gamesLost"),
              "goalsFor" -> JsString("$goalsFor"),
              "goalsAgainst" -> JsString("$goalsAgainst"),
              "goalDifference" -> Json.obj("$subtract" -> Seq("$goalsFor", "$goalsAgainst")),
              "points" -> Json.obj("$add" -> Seq(
                Json.obj("$multiply" -> Seq(JsString("$gamesWon"), JsNumber(victoryPoint))),
                Json.obj("$multiply" -> Seq(JsString("$gamesDrawn"), JsNumber(drawPoint)))
              ))
            )).makePipe
          ))
        )),
        List(
          Project(Json.obj("standings" -> Json.obj("$concatArrays" -> Seq("$homeTeamStandings", "$awayTeamStandings")))),
          UnwindField("standings"),
          ReplaceRootField("standings"),
          Group(JsString("$_id"))(
            "gamesPlayed" -> SumField("gamesPlayed"),
            "goalsFor" -> SumField("goalsFor"),
            "goalsAgainst" -> SumField("goalsAgainst"),
            "goalDifference" -> SumField("goalDifference"),
            "gamesWon" -> SumField("gamesWon"),
            "gamesDrawn" -> SumField("gamesDrawn"),
            "gamesLost" -> SumField("gamesLost"),
            "points" -> SumField("points")
          ),
          PipelineOperator(Json.obj(
            "$facet" -> Json.obj(
              "items" -> Seq(
                Sort(Descending("points"), Descending("goalDifference")).makePipe,
                Group(JsNull)("items" -> PushField("$ROOT")).makePipe,
                Unwind("items", Option("position"), Option(false)).makePipe,
                Project(Json.obj(fields =
                  "team" -> JsString("$items._id"),
                  "gamesPlayed" -> JsString("$items.gamesPlayed"),
                  "goalsFor" -> JsString("$items.goalsFor"),
                  "goalsAgainst" -> JsString("$items.goalsAgainst"),
                  "goalDifference" -> JsString("$items.goalDifference"),
                  "gamesWon" -> JsString("$items.gamesWon"),
                  "gamesDrawn" -> JsString("$items.gamesDrawn"),
                  "gamesLost" -> JsString("$items.gamesLost"),
                  "points" -> JsString("$items.points"),
                  "position" -> Json.obj("$sum" -> Seq(JsString("$position"), JsNumber(1)))
                )).makePipe,
                Skip(limit * (offset - 1)).makePipe,
                Limit(limit).makePipe
              ),
              "pageInfo" -> Seq(Group(JsNull)("total" -> SumAll).makePipe)
            )
          )),
          UnwindField("pageInfo"),
          Project(Json.obj(fields =
            "total" -> JsString("$pageInfo.total"),
            "items" -> JsString("$items")
          )),
          AddFields(Json.obj(fields =
            "offset" -> JsNumber(offset),
            "limit" -> JsNumber(limit)
          ))
        )
      ).prepared.cursor.head
    })
  }
}
