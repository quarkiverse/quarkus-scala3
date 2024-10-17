
package io.quarkiverse.scala.scala3.zio.it

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import zio.*

@Path("/scala3-zio")
@ApplicationScoped
class Scala3ZioResource {
    // add some rest methods here

    @GET
    @Path("")
    def hello(): String = "Hello scala3-zio simple string"

    @GET
    @Path("/zio-string")
    def zioString: ZIO[String with Int, CustomThrowable, String] = {
        import zio._
        for {
          _ <- ZIO.sleep(2.seconds)
        } yield "Hello ZIO"
      }
  
  
    @GET
    @Path("/zio-task")
    def zioTask(@QueryParam("a") a: String): Task[String] = {
      import zio._
      for {
        _ <- ZIO.unit
      } yield s"Hello ZIO Task: ${a}"
    }
  
    @GET
    @Path("/zio-uio")
    def zioUIO(@QueryParam("a") a: String): UIO[String] = {
      import zio._
      for {
        _ <- ZIO.unit
      } yield s"Hello ZIO UIO: ${a}"
    }
    
    @GET
    @Path("/zio-io")
    def zioIO(@QueryParam("a") a: String): IO[CustomThrowable, String] = {
      import zio._
      for {
        _ <- ZIO.unit
      } yield s"Hello ZIO IO: ${a}"
    }
    
//    @GET
//    @Path("/zio-wrong-error-type")
//    def zioError(a: String): ZIO[Any, Nothing, String] = {
//      import zio._
//      for {
//        _ <- ZIO.unit
//      } yield "Hello ZIO"
//    }

}
