package com.example.demo.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class AuthenticationE2ETest extends E2ESupport {

  @Test
  void ottGenerate_sendsEmailToRecipient() {
    String email = uniqueEmail();

    RestAssured.given()
      .baseUri(BASE_URL)
      .contentType(ContentType.URLENC)
      .formParam("username", email)
      .post("/ott/generate")
      .then()
      .statusCode(200);

    RestAssured.given()
      .baseUri(MAILPIT_URL)
      .queryParam("query", "to:" + email)
      .get("/api/v1/search")
      .then()
      .statusCode(200)
      .body("messages", hasSize(1))
      .body("messages[0].To[0].Address", equalTo(email));
  }

  @Test
  void login_withTokenFromEmail_createsSession() {
    RequestSpecification spec = login(uniqueEmail());

    spec.get("/api/v1/me").then().statusCode(200);
  }

  @Test
  void logout_invalidatesSession() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .contentType(ContentType.URLENC)
      .redirects()
      .follow(false)
      .post("/logout")
      .then()
      .statusCode(200);

    spec.redirects().follow(false).get("/api/v1/me").then().statusCode(302);
  }

  @Test
  void login_withInvalidToken_redirectsToError() {
    unauthenticated()
      .contentType(ContentType.URLENC)
      .formParam("token", UUID.randomUUID().toString())
      .post("/login/ott")
      .then()
      .statusCode(302)
      .header("Location", containsString("error"));
  }

  @Test
  void protectedEndpoint_withoutSession_redirects() {
    unauthenticated().get("/api/v1/me").then().statusCode(302);
  }

  @Test
  void login_setsPersistentSessionCookie() {
    String email = uniqueEmail();

    RestAssured.given()
      .baseUri(BASE_URL)
      .contentType(ContentType.URLENC)
      .formParam("username", email)
      .post("/ott/generate")
      .then()
      .statusCode(200);

    String messageId = RestAssured.given()
      .baseUri(MAILPIT_URL)
      .queryParam("query", "to:" + email)
      .get("/api/v1/search")
      .then()
      .statusCode(200)
      .extract()
      .<String>path("messages[0].ID");

    String body = RestAssured.given()
      .baseUri(MAILPIT_URL)
      .get("/api/v1/message/" + messageId)
      .then()
      .statusCode(200)
      .extract()
      .<String>path("HTML");

    Matcher m = Pattern.compile("token=([a-f0-9\\-]{36})").matcher(body);

    if (!m.find()) {
      throw new AssertionError("Token not found in email body: " + body);
    }

    String token = m.group(1);

    String setCookie = RestAssured.given()
      .baseUri(BASE_URL)
      .contentType(ContentType.URLENC)
      .formParam("token", token)
      .redirects()
      .follow(false)
      .post("/login/ott")
      .then()
      .statusCode(302)
      .extract()
      .header("Set-Cookie");

    assertThat(setCookie, containsString("SESSION="));
    assertThat(setCookie, containsString("Max-Age=604800"));
  }

  @Test
  void authenticatedRequest_refreshesSessionCookieMaxAge() {
    RequestSpecification spec = login(uniqueEmail());

    String setCookie = spec
      .redirects()
      .follow(false)
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .extract()
      .header("Set-Cookie");

    assertThat(setCookie, containsString("SESSION="));
    assertThat(setCookie, containsString("Max-Age=604800"));
  }
}
