package repositories

import models.{FootballMatch, TeamStanding}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait FootballMatchRepository {

  def add(footballMatch: FootballMatch): Future[WriteResult]

  def findForWeek(week: Int): Future[Seq[FootballMatch]]

  def getTeamStandings(victoryPoint: Int, drawPoint: Int, offset: Int, limit: Int): Future[Seq[TeamStanding]]

  def countTeams: Future[Int]
}
