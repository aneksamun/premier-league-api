package models

case class TeamStanding(
  position: Int,
  team: String,
  gamesPlayed: Int,
  gamesWon: Int,
  gamesDrawn: Int,
  gamesLost: Int,
  goalsFor: Int,
  goalsAgainst: Int,
  goalDifference: Int,
  points: Int
)
