package example.exports.scala.example_exports

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import example.bindings.wit.*

import java.nio.charset.StandardCharsets

object GlobalState {
  val counters: scala.collection.mutable.Set[Counter] = scala.collection.mutable.Set()
}

class Counter(name0: String) extends example.bindings.exports.scala.example_exports.api.Counter(name0) {
  GlobalState.counters.add(this)

  private var value: Int = 0

  override def increment(delta: Int): Unit = {
    value += delta

    if (value > 100) {
      Counter.sendValue(name0, value)
    }
  }

  override def get(): Int = {
    value
  }

  override def name(): String = {
    name0
  }

}

object Counter extends example.bindings.exports.scala.example_exports.api.CounterStatic {
  @JSExportStatic("getAll")
  override def getAll(): WitList[WitTuple2[String, Int]] = {
    WitList.fromList(GlobalState.counters.map { counter =>
      WitTuple2(counter.name(), counter.get())
    }.toList)
  }

  def sendValue(name: String, value: Int): Unit = {
    import example.bindings.wasi.http.v0_2_0.types._
    import example.bindings.wasi.http.v0_2_0.types.Types._
    import example.bindings.wasi.http.v0_2_0.outgoing_handler._

    println(s"Counter $name reached value $value, publishing via http...")

    val headers = new Fields()
    headers.append("Content-Type", WitList.fromList("application/json".getBytes(StandardCharsets.UTF_8).toList))
    val request = new OutgoingRequest(headers)

    val result = for {
      _ <- request.setMethod(Method.post()).toEither
      _ <- request.setScheme(Nullable.some(Scheme.http())).toEither
      _ <- request.setAuthority(Nullable.some("localhost:8888")).toEither
      _ <- request.setPathWithQuery(Nullable.some("/counter")).toEither
      body <- request.body().toEither
      stream <- body.write().toEither
      _ <- stream.write(WitList.fromList(s"""{"name": "$name", "value": $value}""".getBytes(StandardCharsets.UTF_8).toList)).toEither
      futureResponse <- OutgoingHandler.handle(request, Nullable.none).toEither
      pollable = futureResponse.subscribe()
      _ = pollable.block()
      response0 <- futureResponse.get().toOption.toRight("Failed to get response")
      response1 <- response0.toEither
      response2 <- response1.toEither
      status = response2.status()
      incomingBody <- response2.consume().toEither
      incomingStream <- incomingBody.stream().toEither
      incomingData <- incomingStream.read(1024).toEither
      incomingString = new String(incomingData.toArray, StandardCharsets.UTF_8)
    } yield (status, incomingString)

    result match
      case Left(failure) => println(s"Failed to send counter value: $failure")
      case Right((status, response)) => println(s"Successfully sent counter value - status: $status, response: $response")
  }
}


@JSExportTopLevel("api")
object Api extends example.bindings.exports.scala.example_exports.api.Api {
  @JSExport("getCounters")
  override def getCounters(): WitList[example.bindings.exports.scala.example_exports.api.Counter] = {
    WitList.fromList(GlobalState.counters.toList)
  }

  @JSExport("Counter")
  def counter(name0: String): example.bindings.exports.scala.example_exports.api.Counter =
    new Counter(name0)

  @JSExport("hello")
  override def hello(): String = {
    Counter.sendValue("hello", 42)
    "hello world"
  }
}
