package controllers

import binders.PagingParams
import javax.inject._
import models.JsonFormats._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.TableService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TableController @Inject()(cc: ControllerComponents, tableService: TableService)
  extends AbstractController(cc)
    with I18nSupport {

  def index(pagingParams: PagingParams) = Action.async {
    tableService.getTable(pagingParams.offset, pagingParams.limit)
      .map { pagedTable => Ok(Json.toJson(pagedTable)) }
  }

  def display(pagingParams: PagingParams) = Action.async { implicit request =>
    tableService.getTable(pagingParams.offset, pagingParams.limit)
      .map { pagedTable => Ok(views.html.renderTable(pagedTable)) }
  }
}
