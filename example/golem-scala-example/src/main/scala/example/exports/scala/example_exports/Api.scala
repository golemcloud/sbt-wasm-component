package example.exports.scala.example_exports

import scala.scalajs.js
import scala.scalajs.js.annotation._
import example.bindings.wit._


class Counter(name0: String) extends example.bindings.exports.scala.example_exports.api.Counter(name0) {
  override def increment(delta: Int): Unit = {
    ???
  }
  override def get(): Int = {
    ???
  }
  override def name(): String = {
    ???
  }

}

object Counter extends example.bindings.exports.scala.example_exports.api.CounterStatic {
  @JSExportStatic("getAll")
  override def getAll(): WitList[WitTuple2[String, Int]] = {
    ???
  }

}


@JSExportTopLevel("api")
object Api extends example.bindings.exports.scala.example_exports.api.Api {
  @JSExport("getCounters")
  override def getCounters(): WitList[example.bindings.exports.scala.example_exports.api.Counter] = {
    ???
  }

  @JSExport("Counter")
  def counter(name0: String): example.bindings.exports.scala.example_exports.api.Counter = 
    new Counter(name0)

}
