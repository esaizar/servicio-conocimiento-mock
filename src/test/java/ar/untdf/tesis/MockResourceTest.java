package ar.untdf.tesis;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MockResourceTest {
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

    @Test
    void testNormativaTramiteConFiltroVigencia() {
        given()
          .when().get("/normativa_tramite/127?vigente_a=2010-06-01")
          .then()
             .statusCode(200)
             .body("", hasSize(1))
             .body("[0].fuente.norma_id", equalTo("ORD-3501"));
    }

    @Test
    void testNormativaTramiteSinFiltroVigencia() {
        given()
          .when().get("/normativa_tramite/127")
          .then()
             .statusCode(200)
             .body("", hasSize(1))
             .body("[0].fuente.norma_id", equalTo("ORD-3500"));
    }
}