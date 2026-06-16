package com.example.demo.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.example.demo.BaseIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class AuthenticationIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void ottGenerate_withValidEmail_returns200() {
    unauthenticatedSpec()
      .contentType(ContentType.URLENC)
      .formParam("username", "user@example.com")
      .post("/ott/generate")
      .then()
      .statusCode(200);
  }

  @Test
  void ottLogin_withValidToken_createsSession() {
    String email = "login@example.com";
    String token = UUID.randomUUID().toString();
    this.jdbcTemplate.update("INSERT INTO users (email) VALUES (?)", email);
    this.jdbcTemplate.update(
      "INSERT INTO one_time_tokens (token_value, username, expires_at) VALUES (?, ?, ?)",
      token,
      email,
      LocalDateTime.now().plusMinutes(5)
    );

    unauthenticatedSpec()
      .contentType(ContentType.URLENC)
      .formParam("token", token)
      .redirects()
      .follow(false)
      .post("/login/ott")
      .then()
      .statusCode(302)
      .cookie("SESSION", notNullValue());
  }

  @Test
  void logout_withAuthenticatedSession_invalidatesSession() {
    RequestSpecification spec = loginAndGetSpec("logout@example.com");

    spec
      .contentType(ContentType.URLENC)
      .redirects()
      .follow(false)
      .post("/logout")
      .then()
      .statusCode(302);
    spec
      .redirects()
      .follow(false)
      .get("/api/v1/me")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void ottLogin_withInvalidToken_fails() {
    unauthenticatedSpec()
      .contentType(ContentType.URLENC)
      .formParam("token", "not-a-real-token")
      .redirects()
      .follow(false)
      .post("/login/ott")
      .then()
      .statusCode(equalTo(302))
      .header("Location", containsString("error"));
  }

  @Test
  void ottLogin_withExpiredToken_fails() {
    String email = "expired@example.com";
    String token = UUID.randomUUID().toString();
    this.jdbcTemplate.update("INSERT INTO users (email) VALUES (?)", email);
    this.jdbcTemplate.update(
      "INSERT INTO one_time_tokens (token_value, username, expires_at) VALUES (?, ?, ?)",
      token,
      email,
      LocalDateTime.now().minusMinutes(1)
    );

    unauthenticatedSpec()
      .contentType(ContentType.URLENC)
      .formParam("token", token)
      .redirects()
      .follow(false)
      .post("/login/ott")
      .then()
      .statusCode(equalTo(302))
      .header("Location", containsString("error"));
  }

  @Test
  void protectedEndpoint_whenUnauthenticated_isRejected() {
    unauthenticatedSpec().get("/api/v1/me").then().statusCode(equalTo(302));
  }
}
