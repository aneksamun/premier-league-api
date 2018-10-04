package models

object JsonFormats {

  import play.api.libs.json._

  implicit val footballMatchFormat: OFormat[FootballMatch] = Json.format[FootballMatch]
  implicit val gameResultFormat: OFormat[GameResult] = Json.format[GameResult]
  implicit val teamStandingFormat: OFormat[TeamStanding] = Json.format[TeamStanding]
  implicit val pageFormat: OFormat[Page[TeamStanding]] = Json.format[Page[TeamStanding]]
}
