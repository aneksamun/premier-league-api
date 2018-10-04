package models

case class FootballMatch(gameWeek: Int, homeTeam: String, awayTeam: String, homeGoals: Int, awayGoals: Int) {

  def result: GameResult = GameResult(
    homeTeam,
    awayTeam,
    homeGoals,
    awayGoals,
    summary
  )

  def summary: String = {
    homeGoals compare awayGoals match {
      case result if result > 0 => "Home Win"
      case result if result < 0 => "Away Win"
      case result if result == result => "Draw"
    }
  }
}
