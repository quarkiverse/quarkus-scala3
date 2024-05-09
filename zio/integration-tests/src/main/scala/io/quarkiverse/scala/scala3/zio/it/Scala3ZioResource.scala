
package io.quarkiverse.scala.scala3.zio.it

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import zio.ZIO

@Path("/scala3-zio")
@ApplicationScoped
class Scala3ZioResource {
    // add some rest methods here

    @GET
    @Path("")
    def hello(): String = "Hello scala3-zio simple string"

    @GET
    @Path("/zio-string")
    def zioString: ZIO[String with Int, Throwable, String] = {
        import zio._
        for {
          _ <- ZIO.sleep(2.seconds)
        } yield "Hello ZIO"
      }


}
