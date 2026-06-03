package com.example.demo.artist;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.example.demo.BaseIntegrationTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

class LikedArtistIntegrationTest extends BaseIntegrationTest {

  @Test
  void getLikedArtists_whenEmpty_returnsEmptyList() {
    loginAndGetSpec("user@example.com")
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void addLikedArtist_withValidName_returns201WithArtist() {
    loginAndGetSpec("user@example.com")
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(201)
      .body("id", notNullValue())
      .body("name", equalTo("Radiohead"))
      .body("addedAt", notNullValue());
  }

  @Test
  void getLikedArtists_afterAdding_returnsArtist() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists");
    spec
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].name", equalTo("Radiohead"));
  }

  @Test
  void deleteLikedArtist_withValidId_returns204() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    int id = spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .extract()
      .path("id");

    spec.delete("/api/v1/liked-artists/" + id).then().statusCode(204);
    spec.get("/api/v1/liked-artists").then().statusCode(200).body("$", empty());
  }

  @Test
  void deleteLikedArtist_ofOtherUser_returns204AndDoesNothing() {
    RequestSpecification userA = loginAndGetSpec("a@example.com");
    RequestSpecification userB = loginAndGetSpec("b@example.com");
    int artistId = userA
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .extract()
      .path("id");

    // User B tries to delete user A's artist
    userB.delete("/api/v1/liked-artists/" + artistId).then().statusCode(204);

    // Artist is still present for user A
    userA
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("$", hasSize(1));
  }

  @Test
  void getLikedArtists_userIsolation() {
    RequestSpecification userA = loginAndGetSpec("a@example.com");
    RequestSpecification userB = loginAndGetSpec("b@example.com");

    userA
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists");
    userB
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void addLikedArtist_withBlankName_returns400() {
    loginAndGetSpec("user@example.com")
      .body(
        """
        {"name": ""}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(400);
  }

  @Test
  void addLikedArtist_duplicate_returns409() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists");
    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(409);
  }

  @Test
  void deleteLikedArtist_nonExistentId_returns204() {
    loginAndGetSpec("user@example.com")
      .delete("/api/v1/liked-artists/99999")
      .then()
      .statusCode(204);
  }

  @Test
  void addLikedArtist_withoutCsrfToken_returns403() {
    loginAndGetSpecWithoutCsrf("user@example.com")
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(equalTo(403));
  }

  @Test
  void deleteLikedArtist_withoutCsrfToken_returns403() {
    loginAndGetSpecWithoutCsrf("user@example.com")
      .delete("/api/v1/liked-artists/1")
      .then()
      .statusCode(equalTo(403));
  }

  @Test
  void getLikedArtists_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void addLikedArtist_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void deleteLikedArtist_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .delete("/api/v1/liked-artists/1")
      .then()
      .statusCode(equalTo(302));
  }
}
