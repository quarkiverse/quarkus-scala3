package io.quarkiverse.scala.scala3.zio.it;


import org.hamcrest.Matchers.is
import org.junit.jupiter.api.Test
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.Matchers

@QuarkusTest
class Scala3ZioResourceTest {


  @Test
  def `test Hello Endpoint`(): Unit = {
    Given {
      _.params("something", "value")
    }.When {
      _.get("/scala3-zio").prettyPeek()
    }.Then {
      _.statusCode(200).body(is("Hello scala3-zio simple string"))
    }
  }

  @Test
  def `test zio-string Endpoint`(): Unit = {
    println("running `test zio-string Endpoint`")
    Given {
      _.params("something", "value")
    }.When {
      _.get("/scala3-zio/zio-string").prettyPeek()
    }.Then {
      _.statusCode(200).body(is("Hello ZIO"))
    }
  }

}
