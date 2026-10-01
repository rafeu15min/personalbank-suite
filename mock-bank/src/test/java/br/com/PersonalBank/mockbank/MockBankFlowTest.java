package br.com.PersonalBank.mockbank;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.net.URI;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MockBankFlowTest {

    private static final String REDIRECT_URI = "http://localhost:9999/callback";

    @Test
    void fullConsentFlowGivesAccessToData() {
        String clientToken = given().formParam("grant_type", "client_credentials")
                .when().post("/token")
                .then().statusCode(200)
                .extract().path("access_token");

        String consentId = given().header("Authorization", "Bearer " + clientToken)
                .contentType("application/json").body("{}")
                .when().post("/open-banking/consents/v3/consents")
                .then().statusCode(201)
                .body("data.status", equalTo("AWAITING_AUTHORISATION"))
                .extract().path("data.consentId");

        Response redirect = given().redirects().follow(false)
                .queryParam("consent_id", consentId)
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "abc")
                .when().get("/authorize");
        assertEquals(303, redirect.statusCode());
        String location = redirect.header("Location");
        assertEquals("abc", queryParam(location, "state"));

        String accessToken = given()
                .formParam("grant_type", "authorization_code")
                .formParam("code", queryParam(location, "code"))
                .when().post("/token")
                .then().statusCode(200)
                .body("access_token", startsWith("at-"))
                .extract().path("access_token");

        given().header("Authorization", "Bearer " + accessToken)
                .when().get("/open-banking/accounts/v2/accounts")
                .then().statusCode(200)
                .body("data", hasSize(2));

        // Um código de autorização só pode ser usado uma vez
        given().formParam("grant_type", "authorization_code")
                .formParam("code", queryParam(location, "code"))
                .when().post("/token")
                .then().statusCode(400)
                .body("error", equalTo("invalid_grant"));
    }

    @Test
    void dataRequiresAuthorisedToken() {
        given().when().get("/open-banking/accounts/v2/accounts")
                .then().statusCode(401);

        String clientToken = given().formParam("grant_type", "client_credentials")
                .when().post("/token")
                .then().extract().path("access_token");
        given().header("Authorization", "Bearer " + clientToken)
                .when().get("/open-banking/accounts/v2/accounts")
                .then().statusCode(401);
    }

    private static String queryParam(String uri, String name) {
        for (String pair : URI.create(uri).getQuery().split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv[0].equals(name)) {
                return kv[1];
            }
        }
        throw new AssertionError("Parâmetro ausente: " + name + " em " + uri);
    }
}
