package controllers

import binders.PagingParams
import javax.inject._
import models.JsonFormats._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import repositories.FootballMatchRepository

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TableController @Inject()(cc: ControllerComponents, configuration: Configuration, repository: FootballMatchRepository)
  extends AbstractController(cc)
    with I18nSupport {

  private val drawPoints = configuration.get[Int]("draw.points")
  private val victoryPoints = configuration.get[Int]("victory.points")

  def index(pagingParams: PagingParams) = Action.async {
    repository.getTable(pagingParams.offset, pagingParams.limit, victoryPoints, drawPoints)
      .map { pagedTable => Ok(Json.toJson(pagedTable)) }
  }

  def display(pagingParams: PagingParams) = Action.async { implicit request =>
    repository.getTable(pagingParams.offset, pagingParams.limit, victoryPoints, drawPoints)
      .map { pagedTable => Ok(views.html.display(pagedTable)) }
  }
}
