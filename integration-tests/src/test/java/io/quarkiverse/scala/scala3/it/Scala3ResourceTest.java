package io.quarkiverse.scala.scala3.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class Scala3ResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/scala3")
                .then()
                .statusCode(200)
                .body(is("Hello scala3"));
    }
}
