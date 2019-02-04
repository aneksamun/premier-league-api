package services

import javax.inject.{Inject, Singleton}
import models.{FootballMatch, GameResult}
import reactivemongo.api.commands.WriteResult
import repositories.FootballMatchRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FootballMatchService @Inject()(footballMatchRepository: FootballMatchRepository) {

  def add(footballMatch: FootballMatch): Future[WriteResult] =
    footballMatchRepository add footballMatch

  def getResults(week: Int): Future[Seq[GameResult]] =
    footballMatchRepository.findForWeek(week)
      .map { _.map { _.result } }
}
