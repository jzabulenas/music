package com.example.demo.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

  static final String DB_URL = System.getenv().getOrDefault(
    "DB_URL",
    "jdbc:mariadb://localhost:3307/music"
  );

  static final String DB_USERNAME = System.getenv().getOrDefault(
    "DB_USERNAME",
    "music"
  );

  static final String DB_PASSWORD = System.getenv().getOrDefault(
    "DB_PASSWORD",
    "music"
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

  // Promotes a user to admin the same way production would: a manual SQL update.
  static void promoteToAdmin(String email) {
    try (
      Connection connection = DriverManager.getConnection(
        DB_URL,
        DB_USERNAME,
        DB_PASSWORD
      );
      PreparedStatement statement = connection.prepareStatement(
        "UPDATE users SET role = 'ADMIN' WHERE email = ?"
      )
    ) {
      statement.setString(1, email);

      if (statement.executeUpdate() != 1) {
        throw new AssertionError("No user found to promote: " + email);
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to promote user: " + email, e);
    }
  }

  static void addLikedArtists(RequestSpecification spec, String... names) {
    for (String name : names) {
      spec
        .body(
          """
          {"name": "%s"}
          """.formatted(name)
        )
        .post("/api/v1/liked-artists")
        .then()
        .statusCode(201);
    }
  }
}
