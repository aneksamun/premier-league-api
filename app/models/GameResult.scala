package models

case class GameResult(
  homeTeam: String,
  awayTeam: String,
  homeGoals: Int,
  awayGoals: Int,
  result: String
)
