package example

trait Api {
  import scala.scalajs.js
  import scala.scalajs.js.JSConverters._

  type WitResult[+Ok, +Err] = Ok
  object WitResult {
    def ok[Ok](value: Ok): WitResult[Ok, Nothing] = value

    def err[Err](value: Err): WitResult[Nothing, Err] = throw js.JavaScriptException(value)

    val unit: WitResult[Unit, Nothing] = ()
  }

  def initializeCart(userId: String): WitResult[String, String]
}