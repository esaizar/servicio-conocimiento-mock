package ar.untdf.tesis;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testHealthEndpoint() {
        given()
          .when().get("/health")
          .then()
             .statusCode(200)
             .body("status", is("ok"));
    }

    @Test
    void testCandidatosEndpoint() {
        given()
          .contentType("application/json")
          .body("{\"texto\":\"necesito un libre deuda\",\"k\":2}")
          .when().post("/candidatos_de_tramite")
          .then()
             .statusCode(200)
             .body("", hasSize(2))
             .body("[0].cod", equalTo(131));
    }
}