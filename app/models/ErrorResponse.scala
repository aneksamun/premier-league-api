package models

case class ErrorResponse(errors: Seq[String])

object ErrorResponse {

  def apply(message: String): ErrorResponse = {
    new ErrorResponse(Seq(message))
  }

  def apply(throwable: Throwable): ErrorResponse = {
    apply(throwable.getMessage)
  }
}
