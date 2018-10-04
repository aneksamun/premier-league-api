import javax.inject._
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.routing.Router

@Singleton
class ErrorHandler @Inject()(
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

//  override def onBadRequest(request: RequestHeader, message: String): Future[Result] =
//    Future.successful(
//      BadRequest(Json.toJson(Json.obj(
//        "error" -> message
//      ))
//    ))
//
//  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
//    Future.successful(
//      InternalServerError("Sleep!!!!!")
//    )
}
