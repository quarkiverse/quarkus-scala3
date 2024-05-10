package io.quarkiverse.scala.scala3.futures.it

import org.hamcrest.Matchers.is
import org.junit.jupiter.api.Test
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.Matchers

@QuarkusTest
class Scala3FuturesResourceTest {



  @Test
  def `test Hello Endpoint`(): Unit = {
    Given {
      _.params("something", "value")
    }.When {
      _.get("/hello")
    }.Then {
      _.statusCode(200).body(is("Hello from Scala 3.4.1"))
    }
  }

  @Test
  def `test future Endpoint`(): Unit = {
    Given {
      _.params("something", "value")
    }.When {
      _.get("/simple-future")
    }.Then {
      _.statusCode(200).body(is("Hello from a Future in Scala 3.4.1"))
    }
  }

  @Test
  def `test promise Endpoint`(): Unit = {
    Given {
      _.params("something", "value")
    }.When {
      _.get("/simple-promise")
    }.Then {
      _.statusCode(200).body(is("Promise returned"))
    }
  }

  @Test
  def `future should fail`(): Unit = {
    Given {
      _.params("something", "value")
    }.When {
      _.get("/future-failure").prettyPeek()
    }.Then {
      _.statusCode(500)
      // body/stack not available in native image?
      //.body(Matchers.containsString("Future failed"))
    }
  }


}
