package com.example.demo.user;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.example.demo.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

class UserIntegrationTest extends BaseIntegrationTest {

  @Test
  void getMe_whenAuthenticated_returnsUserEmailAndId() {
    loginAndGetSpec("me@example.com")
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .body("id", notNullValue())
      .body("email", equalTo("me@example.com"));
  }

  @Test
  void getMe_whenUnauthenticated_isRejected() {
    unauthenticatedSpec().get("/api/v1/me").then().statusCode(equalTo(302));
  }
}
