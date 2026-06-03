package com.example.demo.saved;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.example.demo.BaseIntegrationTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

class SavedArtistIntegrationTest extends BaseIntegrationTest {

  @Test
  void getSavedArtists_whenEmpty_returnsEmptyList() {
    loginAndGetSpec("user@example.com")
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void saveArtist_withNameOnly_returns201WithNullGenre() {
    loginAndGetSpec("user@example.com")
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201)
      .body("id", notNullValue())
      .body("name", equalTo("Portishead"))
      .body("genre", nullValue())
      .body("savedAt", notNullValue());
  }

  @Test
  void saveArtist_withNameAndGenre_returns201WithGenre() {
    loginAndGetSpec("user@example.com")
      .body(
        """
        {"name": "Portishead", "genre": "Trip-hop"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201)
      .body("name", equalTo("Portishead"))
      .body("genre", equalTo("Trip-hop"));
  }

  @Test
  void getSavedArtists_afterSaving_returnsArtist() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");

    spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists");
    spec
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].name", equalTo("Portishead"));
  }

  @Test
  void deleteSavedArtist_withValidId_returns204() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    int id = spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .extract()
      .path("id");

    spec.delete("/api/v1/saved-artists/" + id).then().statusCode(204);
    spec.get("/api/v1/saved-artists").then().statusCode(200).body("$", empty());
  }

  @Test
  void getSavedArtists_userIsolation() {
    RequestSpecification userA = loginAndGetSpec("a@example.com");
    RequestSpecification userB = loginAndGetSpec("b@example.com");

    userA
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists");
    userB
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void saveArtist_withBlankName_returns400() {
    loginAndGetSpec("user@example.com")
      .body(
        """
        {"name": ""}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(400);
  }

  @Test
  void saveArtist_duplicate_returns409() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");

    spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists");
    spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(409);
  }

  @Test
  void deleteSavedArtist_nonExistentId_returns204() {
    loginAndGetSpec("user@example.com")
      .delete("/api/v1/saved-artists/99999")
      .then()
      .statusCode(204);
  }

  @Test
  void saveArtist_withoutCsrfToken_returns403() {
    loginAndGetSpecWithoutCsrf("user@example.com")
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(equalTo(403));
  }

  @Test
  void deleteSavedArtist_withoutCsrfToken_returns403() {
    loginAndGetSpecWithoutCsrf("user@example.com")
      .delete("/api/v1/saved-artists/1")
      .then()
      .statusCode(equalTo(403));
  }

  @Test
  void getSavedArtists_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void saveArtist_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void deleteSavedArtist_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .delete("/api/v1/saved-artists/1")
      .then()
      .statusCode(equalTo(302));
  }
}
