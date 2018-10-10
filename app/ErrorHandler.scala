import javax.inject._
import models.ErrorResponse
import models.JsonFormats.errorResponseWriter
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject()(
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] =
    Future.successful(
      BadRequest(Json.toJson(ErrorResponse(message.split(";"))))
    )

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      InternalServerError(Json.toJson(ErrorResponse(exception)))
    )
  }
}
