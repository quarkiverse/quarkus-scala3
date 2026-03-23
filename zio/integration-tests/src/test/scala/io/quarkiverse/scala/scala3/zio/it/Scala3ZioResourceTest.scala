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
    Given {
      _.params("something", "value")
    }.When {
      _.get("/scala3-zio/zio-string").prettyPeek()
    }.Then {
      _.statusCode(200).body(is("Hello ZIO"))
    }
  }

  @Test
  def `test zio Task Endpoint`(): Unit = {
    Given {
      _.params("a", "value")
    }.When {
      _.get("/scala3-zio/zio-task")
    }.Then {
      _.statusCode(200).body(is("Hello ZIO Task: value"))
    }
  }

  @Test
  def `test zio UIO Endpoint`(): Unit = {
    Given {
      _.params("a", "value")
    }.When {
      _.get("/scala3-zio/zio-uio")
    }.Then {
      _.statusCode(200).body(is("Hello ZIO UIO: value"))
    }
  }

  @Test
  def `test zio IO Endpoint`(): Unit = {
    Given {
      _.params("a", "value")
    }.When {
      _.get("/scala3-zio/zio-io")
    }.Then {
      _.statusCode(200).body(is("Hello ZIO IO: value"))
    }
  }
  
  
  

}
