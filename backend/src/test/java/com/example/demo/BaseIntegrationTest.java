package com.example.demo;

import com.example.demo.recommendation.ai.ArtistRecommendationClient;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockitoBean
  private JavaMailSender mailSender;

  // Placed here so all test classes share one Spring context.
  // Tests that do not exercise recommendations simply leave the mock unconfigured.
  @MockitoBean
  protected ArtistRecommendationClient recommendationClient;

  @BeforeEach
  void setUp() {
    this.jdbcTemplate.update("DELETE FROM liked_artists");
    this.jdbcTemplate.update("DELETE FROM recommendations");
    this.jdbcTemplate.update("DELETE FROM saved_artists");
    this.jdbcTemplate.update("DELETE FROM one_time_tokens");
    this.jdbcTemplate.update("DELETE FROM users");

    // MagicLinkSuccessHandler calls createMimeMessage() before send(). A plain
    // Mockito mock returns null for object methods, which causes a NullPointerException
    // inside MimeMessageHelper. Returning a real MimeMessage with an empty session
    // lets the helper populate fields normally; send() is still a no-op.
    Session session = Session.getDefaultInstance(new Properties());
    Mockito.when(this.mailSender.createMimeMessage()).thenReturn(
      new MimeMessage(session)
    );
  }

  // Inserts a user and OTT token directly into the DB, consumes the token via
  // POST /login/ott, then returns a spec pre-loaded with SESSION + XSRF cookies.
  //
  // CookieCsrfTokenRepository (SPA mode) is stateless: it validates that the
  // X-XSRF-TOKEN header value matches the XSRF-TOKEN cookie value. Supplying a
  // self-generated UUID for both satisfies the check without involving a browser.
  protected RequestSpecification loginAndGetSpec(String email) {
    String ottToken = UUID.randomUUID().toString();
    this.jdbcTemplate.update("INSERT INTO users (email) VALUES (?)", email);
    this.jdbcTemplate.update(
      "INSERT INTO one_time_tokens (token_value, username, expires_at) VALUES (?, ?, ?)",
      ottToken,
      email,
      LocalDateTime.now().plusMinutes(5)
    );

    Response loginResponse = RestAssured.given()
      .baseUri("http://localhost:" + this.port)
      .contentType(ContentType.URLENC)
      .formParam("token", ottToken)
      .redirects()
      .follow(false)
      .post("/login/ott");

    String sessionCookie = loginResponse.cookie("SESSION");
    String csrfToken = UUID.randomUUID().toString();

    return RestAssured.given()
      .baseUri("http://localhost:" + this.port)
      .cookie("SESSION", sessionCookie)
      .cookie("XSRF-TOKEN", csrfToken)
      .header("X-XSRF-TOKEN", csrfToken)
      .contentType(ContentType.JSON);
  }

  protected RequestSpecification loginAndGetSpecWithoutCsrf(String email) {
    String ottToken = UUID.randomUUID().toString();
    this.jdbcTemplate.update("INSERT INTO users (email) VALUES (?)", email);
    this.jdbcTemplate.update(
      "INSERT INTO one_time_tokens (token_value, username, expires_at) VALUES (?, ?, ?)",
      ottToken,
      email,
      LocalDateTime.now().plusMinutes(5)
    );

    Response loginResponse = RestAssured.given()
      .baseUri("http://localhost:" + this.port)
      .contentType(ContentType.URLENC)
      .formParam("token", ottToken)
      .redirects()
      .follow(false)
      .post("/login/ott");

    String sessionCookie = loginResponse.cookie("SESSION");

    return RestAssured.given()
      .baseUri("http://localhost:" + this.port)
      .cookie("SESSION", sessionCookie)
      .contentType(ContentType.JSON);
  }

  // Redirects are disabled so that unauthenticated requests return the 302
  // directly rather than following through to the login page (which returns 200).
  // A CSRF token is included so that POST/DELETE tests exercise the authentication
  // check rather than the CSRF filter.
  protected RequestSpecification unauthenticatedSpec() {
    String csrfToken = UUID.randomUUID().toString();

    return RestAssured.given()
      .baseUri("http://localhost:" + this.port)
      .cookie("XSRF-TOKEN", csrfToken)
      .header("X-XSRF-TOKEN", csrfToken)
      .contentType(ContentType.JSON)
      .redirects()
      .follow(false);
  }
}
