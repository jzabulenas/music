package com.example.demo.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class E2ESupport {

  static final String BASE_URL = System.getenv().getOrDefault(
    "BASE_URL",
    "http://localhost:8081"
  );

  static final String MAILPIT_URL = System.getenv().getOrDefault(
    "MAILPIT_URL",
    "http://localhost:8026"
  );

  static String uniqueEmail() {
    return "t+" + UUID.randomUUID().toString().substring(0, 8) + "@x.com";
  }

  // Drives the full magic-link flow via HTTP only.
  // /ott/generate auto-creates the user, sends a real email to Mailpit,
  // then we extract the token from Mailpit's REST API and consume it.
  static RequestSpecification login(String email) {
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

    // group(0) = full match "token=abc...", group(1) = capture group value only
    String token = m.group(1);

    Response loginResp = RestAssured.given()
      .baseUri(BASE_URL)
      .contentType(ContentType.URLENC)
      .formParam("token", token)
      .redirects()
      .follow(false)
      .post("/login/ott")
      .then()
      .statusCode(302)
      .extract()
      .response();

    String csrfToken = UUID.randomUUID().toString();

    return RestAssured.given()
      .baseUri(BASE_URL)
      .cookie("SESSION", loginResp.cookie("SESSION"))
      .cookie("XSRF-TOKEN", csrfToken)
      .header("X-XSRF-TOKEN", csrfToken)
      .contentType(ContentType.JSON);
  }

  static RequestSpecification unauthenticated() {
    String csrfToken = UUID.randomUUID().toString();

    return RestAssured.given()
      .baseUri(BASE_URL)
      .cookie("XSRF-TOKEN", csrfToken)
      .header("X-XSRF-TOKEN", csrfToken)
      .contentType(ContentType.JSON)
      .redirects()
      .follow(false);
  }
}
