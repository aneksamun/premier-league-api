package repositories

import javax.inject.{Inject, Singleton}
import models.{FootballMatch, TeamStanding}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FootballMatchMongoRepository @Inject()(implicit ec: ExecutionContext, api: ReactiveMongoApi)
  extends FootballMatchRepository {

  import models.JsonFormats._

  val footballMatches: Future[JSONCollection] = api.database.map(_.collection(name = "football_matches"))

  footballMatches.map(_.indexesManager.ensure(Index(List(
    "homeTeam" -> Ascending,
    "awayTeam" -> Ascending,
    "gameWeek" -> Ascending), unique = true)
  ))

  override def add(footballMatch: FootballMatch): Future[WriteResult] = {
    footballMatches.flatMap(_.insert(footballMatch))
  }

  override def findForWeek(week: Int): Future[Seq[FootballMatch]] = {
    footballMatches.flatMap(_.find(Json.obj("gameWeek" -> week), Option(Json.obj()))
      .sort(Json.obj("homeTeam" -> 1))
      .cursor[FootballMatch](ReadPreference.primary)
      .collect[Seq](10000, Cursor.FailOnError[Seq[FootballMatch]]())
    )
  }

  override def getTeamStandings(victoryPoint: Int, drawPoint: Int, offset: Int, limit: Int): Future[Seq[TeamStanding]] = {
    footballMatches.flatMap(collection => {
      import collection.BatchCommands.AggregationFramework.{Descending, Group, Limit, PipelineOperator, Project, PushField, ReplaceRootField, Skip, Sort, Sum, SumAll, SumField, Unwind, UnwindField}

      collection.aggregatorContext[TeamStanding](
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
          Sort(Descending("points"), Descending("goalDifference")),
          Group(JsNull)("items" -> PushField("$ROOT")),
          Unwind("items", Option("position"), Option(false)),
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
          )),
          Skip(limit * (offset - 1)),
          Limit(limit)
        )
      ).prepared.cursor.collect[Seq](10000, Cursor.FailOnError[Seq[TeamStanding]]())
    })
  }

  override def countTeams: Future[Int] = {
    footballMatches.flatMap(collection => {
      import collection.BatchCommands.AggregationFramework._

      val maybeDocument = collection.aggregatorContext[BSONDocument](
        Project(Json.obj("items" -> Seq(JsString("$homeTeam"), JsString("$awayTeam")))),
        List(
          UnwindField("items"),
          Group(JsNull)("teams" -> AddFieldToSet("items")),
          UnwindField("teams"),
          Group(JsNull)("count" -> SumAll)
        )
      ).prepared.cursor.headOption

      maybeDocument.map {
        case Some(document) => document.getAs[Int]("count").get
        case None => 0
      }
    })
  }
}
