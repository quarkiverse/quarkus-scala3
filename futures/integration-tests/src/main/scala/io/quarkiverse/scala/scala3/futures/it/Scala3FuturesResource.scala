package io.quarkiverse.scala.scala3.futures.it

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global

@Path("")
class Scala3FuturesResource {

  @GET
  @Path("/hello")
  def hello(): String = "Hello from Scala 3.4.1"

  @GET
  @Path("/simple-future")
  @Produces(Array(TEXT_PLAIN))
  def simpleFuture: Future[String] = for {
    _ <- Future { Thread.sleep(2000L) }
    s <- Future.successful("Hello from a Future in Scala 3.4.1")
  } yield s

  @GET
  @Path("simple-promise")
  @Produces(Array(TEXT_PLAIN))
  def simplePromise: Promise[String] = Promise.successful("Promise returned")
  
  
  @GET
  @Path("future-failure")
  @Produces(Array(TEXT_PLAIN))
  def futureFailure: Future[String] = Future.failed(new RuntimeException("Future failed"))

}
