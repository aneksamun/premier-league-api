import javax.inject._
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n._
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(env: Environment,
                             config: Configuration,
                             sourceMapper: OptionalSourceMapper,
                             router: Provider[Router],
                             api: MessagesApi)
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with I18nSupport {

  override def messagesApi: MessagesApi = api

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    Logger.error(message)
    val details = message.split(";")
    implicit val messages: Messages = request2Messages(request)
    Future.successful(BadRequest(views.html.renderError(details)))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger.error(exception.toString)
    val details = Seq(exception.getMessage)
    implicit val messages: Messages = request2Messages(request)
    Future.successful(InternalServerError(views.html.renderError(details)))
  }
}
