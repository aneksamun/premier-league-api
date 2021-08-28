package controllers

import models.FootballMatch
import models.JsonFormats._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data.{Form, FormError}
import play.api.i18n._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._
import services.FootballMatchService

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class GameController @Inject()(cc: ControllerComponents,
                               footballMatchService: FootballMatchService,
                               messagesApi: MessagesApi,
                               languages: Langs) extends AbstractController(cc) {

  lazy implicit val lang: Lang = languages.availables.head

  implicit object FormErrorWrites extends Writes[FormError] {
    override def writes(error: FormError): JsValue = Json.obj(fields =
      "field" -> Json.toJson(error.key),
      "error" -> Json.toJson(messagesApi(error.message, error.args.headOption.orNull))
    )
  }

  val footballMatchForm = Form {
    mapping(
      "gameWeek" -> number.verifying(min(1), max(38)),
      "homeTeam" -> nonEmptyText,
      "awayTeam" -> nonEmptyText,
      "homeGoals" -> number.verifying(min(0)),
      "awayGoals" -> number.verifying(min(0))
    )(FootballMatch.apply)(FootballMatch.unapply)
  }

  def index(week: Int) = Action.async {
    footballMatchService.getResults(week)
      .map { results => Ok(Json.toJson(results)) }
  }

  def add = Action.async { implicit request =>
    footballMatchForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(formWithErrors.errors))),
      footballMatch => {
        footballMatchService.add(footballMatch).map { _ =>
          Created
        }
      }
    )
  }
}
