package models

object JsonFormats {

  import play.api.libs.json._

  implicit val gameResultFormatter: OFormat[GameResult] = Json.format[GameResult]
  implicit val teamStandingFormatter: OFormat[TeamStanding] = Json.format[TeamStanding]
  implicit val footballMatchFormatter: OFormat[FootballMatch] = Json.format[FootballMatch]
  implicit val pageFormatter: OFormat[Page[TeamStanding]] = Json.format[Page[TeamStanding]]
  implicit val errorResponseWriter: OWrites[ErrorResponse] = Json.writes[ErrorResponse]
}
