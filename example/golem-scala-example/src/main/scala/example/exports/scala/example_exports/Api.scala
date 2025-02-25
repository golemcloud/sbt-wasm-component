package example.exports.scala.example_exports

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters._
import example.bindings.wit.*

import java.nio.charset.StandardCharsets
import scala.scalajs.js.typedarray.Uint8Array

object GlobalState {
  val counters: scala.collection.mutable.Set[Counter] = scala.collection.mutable.Set()
}

@JSExportTopLevel("api_Counter")
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
    headers.append("Content-Type", Uint8Array("application/json".getBytes(StandardCharsets.UTF_8).map(_.toShort).toJSArray))
    val request = new OutgoingRequest(headers)

    request.setMethod(Method.post())

    request.setScheme(Nullable.some(Scheme.http()))
    request.setAuthority(Nullable.some("localhost:8888"))
    request.setPathWithQuery(Nullable.some("/counter"))
    val body = request.body()
    val stream = body.write()
    stream.write(Uint8Array(s"""{"name": "$name", "value": $value}""".getBytes(StandardCharsets.UTF_8).map(_.toShort).toJSArray))
    val futureResponse = OutgoingHandler.handle(request, Nullable.none)
    val pollable = futureResponse.subscribe()
    pollable.block()

    val result =
      for {
        response0 <- futureResponse.get().toOption.toRight("Failed to get response")
        response1 <- response0.toEither
        response2 <- response1.toEither
        status = response2.status()
        incomingBody = response2.consume()
        incomingStream = incomingBody.stream()
        incomingData = incomingStream.read(1024)
        incomingString = new String(incomingData.map(_.toByte).toArray, StandardCharsets.UTF_8)
      } yield (status, incomingString)

    result match {
      case Left(error) =>
        println(s"Failed to send counter value - error: $error")
      case Right((status, response)) =>
        println(s"Successfully sent counter value - status: $status, response: $response")
    }
  }
}

@JSExportTopLevel("api")
object Api extends example.bindings.exports.scala.example_exports.api.Api {
  @JSExport("getCounters")
  override def getCounters(): WitList[WitTuple2[String, Int]] = {
    Counter.getAll()
  }
}
