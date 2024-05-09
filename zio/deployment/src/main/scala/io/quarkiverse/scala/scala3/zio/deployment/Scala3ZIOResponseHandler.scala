package io.quarkiverse.scala.scala3.zio.deployment

import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler
import zio.ZIO

import java.lang.reflect.ParameterizedType
import scala.jdk.CollectionConverters.*
import scala.util.Failure
import scala.util.Success

class Scala3ZIOResponseHandler() extends ServerRestHandler {

  override def handle(requestContext: ResteasyReactiveRequestContext): Unit = {
    val result = requestContext.getResult

    type R = Any
    type E = Throwable
    type A = Any
    /*
    // TODO at the moment, we're just stupidly assume, the effect has no environment
    //  and the error type is a throwable. We need to figure out a way on how to access
    //  the type arguments of the ZIO effect in a more structured way.
    // At the moment, a Environment of e.g. String with Int will be read as java.lang.Object,
    // so we loose the type information which could be used for dependency injection
    println("in handle of Scala3ZIOResponseHandler")

    val p = requestContext.getGenericReturnType.asInstanceOf[ParameterizedType]
    val typeArgs = p.getActualTypeArguments.toSeq
    val (environment, errorType, successType) = (typeArgs(0), typeArgs(1), typeArgs(2))

    println(s"environment: $environment")
    println(s"errorType: $errorType")
    println(s"successType: $successType")
    */
    result match
      case r: ZIO[R, E, A] =>
        requestContext.suspend()
        val f = zio.Unsafe.unsafe(u => zio.Runtime.default.unsafe.runToFuture(r)(zio.Trace.empty, u))
        f.onComplete {
          case Success(value) =>
            requestContext.setResult(value)
            requestContext.resume()
          case Failure(exception) =>
            requestContext.handleException(exception, true)
            requestContext.resume()
        }(scala.concurrent.ExecutionContext.global)

      case _ => ()
  }
}
