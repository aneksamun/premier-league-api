package services

import javax.inject.{Inject, Singleton}
import models.{Page, TeamStanding}
import play.api.Configuration
import repositories.FootballMatchRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TableService @Inject()(footballMatchRepository: FootballMatchRepository,
                             configuration: Configuration) {

  private val drawPoints = configuration.get[Int]("draw.points")
  private val victoryPoints = configuration.get[Int]("victory.points")

  def getTable(offset: Int, limit: Int): Future[Page[TeamStanding]] = {
    for {
      total <- footballMatchRepository.countTeams
      items <- footballMatchRepository.getTeamStandings(victoryPoints, drawPoints, offset, limit)
    } yield Page[TeamStanding](offset, limit, total, items)
  }
}
