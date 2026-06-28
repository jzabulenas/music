package com.example.demo.e2e;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class UserE2ETest extends E2ESupport {

  @Test
  void getMe_returnsAuthenticatedUsersEmail() {
    String email = uniqueEmail();
    RequestSpecification spec = login(email);

    spec
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .body("email", equalTo(email))
      .body("id", notNullValue());
  }
}
