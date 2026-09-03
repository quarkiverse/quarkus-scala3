package io.quarkiverse.scala.scala3.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testScalaSourcesAreCompiledAndServed() {
        given()
                .when().get("/scala3/greeting")
                .then()
                .statusCode(200)
                .body(is("hello from scala"));
    }
}
