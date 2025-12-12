package com.deskpicker.app;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class HealthResourceTest {

  @Test
  void healthEndpointIsUp() {
    given()
        .when()
        .get("/healthz")
        .then()
        .statusCode(200)
        .body("status", is("ok"));
  }

  @Test
  void readinessEndpointIsUp() {
    given()
        .when()
        .get("/readyz")
        .then()
        .statusCode(200)
        .body("status", is("ready"));
  }
}
