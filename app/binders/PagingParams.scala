package binders

import play.api.data.validation._
import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}

case class PagingParams(offset: Int, limit: Int)

object PagingParams {

  private val DefaultOffset = 1
  private val DefaultLimit = 20
  private val OffsetParamKey = "offset"
  private val LimitParamKey = "limit"
  private val ValidationConstraints = Seq(Constraints.min(1), Constraints.max(100000))

  implicit def queryStringBinder(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[PagingParams] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PagingParams]] = {
      val result = for {
        offset <- Try(intBinder.bind(OffsetParamKey, params).get).recover {
          case _ => Right(DefaultOffset)
        }
        limit <- Try(intBinder.bind(LimitParamKey, params).get).recover {
          case _ => Right(DefaultLimit)
        }
      } yield {
        (offset.right.toOption, limit.right.toOption)
      }
      result match {
        case Success((maybeIndex, maybeLimit)) =>
          ParameterValidator(ValidationConstraints, maybeIndex, maybeLimit) match {
            case Valid =>
              Some(Right(PagingParams(maybeIndex.get, maybeLimit.get)))
            case Invalid(errors) =>
              Some(Left(errors.zip(Seq(OffsetParamKey, LimitParamKey)).map {
                case (ValidationError(_, value), param) => s"Minimum allowed value for $param parameter is $value"
              }.mkString(", ")))
          }
        case Failure(e) => Some(Left(s"Invalid paging parameters: ${e.getMessage}"))
      }
    }

    override def unbind(key: String, pagingParams: PagingParams): String =
      intBinder.unbind(OffsetParamKey, pagingParams.offset) + "&" + intBinder.unbind(LimitParamKey, pagingParams.limit)
  }
}
