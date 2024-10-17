package io.quarkiverse.scala.scala3.zio.deployment

import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler
import zio.ZIO

import scala.jdk.CollectionConverters.*

class Scala3ZIOResponseHandler() extends ServerRestHandler {

  override def handle(requestContext: ResteasyReactiveRequestContext): Unit = {
    val result = requestContext.getResult

    /*
      TODO if we're able to read the environment from the effect, we might be able to hook into
      Quarkus dependency injection mechanism to fill it here. For now, we can only assume its any.
     */
    type R = Any

    /* fixing the error type to Throwable. We can be sure its this type, as we've checked
       it before in io.quarkiverse.scala.scala3.zio.deployment.Scala3ZIOReturnTypeMethodScanner.scan
       There it can only be Nothing, or Throwable or subtypes of Throwable, so either way, we're
       safe to assume it's Throwable here.
     */
    type E = Throwable

    /*  We assume any as return type, as quarkus also accepts any object as return type. 
     */
    type A = Any

    requestContext.suspend()
    val r = result.asInstanceOf[ZIO[R, E, A]]
    
    val r1 = r.fold(e => {
      requestContext.handleException(e)
      requestContext.resume()
    }, a => {
        requestContext.setResult(a)
        requestContext.resume()
    })
    
    zio.Unsafe.unsafe(u => zio.Runtime.default.unsafe.runToFuture(r1)(zio.Trace.empty, u))
  }
}
