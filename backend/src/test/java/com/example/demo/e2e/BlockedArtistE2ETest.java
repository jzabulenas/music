package com.example.demo.e2e;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class BlockedArtistE2ETest extends E2ESupport {

  @Test
  void blockArtist_returns204() {
    login(uniqueEmail())
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(204);
  }

  @Test
  void blockArtist_thenList_containsBlockedArtist() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(204);

    spec
      .get("/api/v1/blocked-artists")
      .then()
      .statusCode(200)
      .body("$", hasItem("Radiohead"));
  }

  @Test
  void listBlockedArtists_whenNoneBlocked_isEmpty() {
    login(uniqueEmail())
      .get("/api/v1/blocked-artists")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void blockedArtists_areIsolatedBetweenUsers() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    userA
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(204);

    userB
      .get("/api/v1/blocked-artists")
      .then()
      .statusCode(200)
      .body("$", not(hasItem("Portishead")));
  }

  @Test
  void listBlockedArtists_whenUnauthenticated_redirects() {
    unauthenticated().get("/api/v1/blocked-artists").then().statusCode(302);
  }

  @Test
  void blockArtist_whenAlreadyBlocked_isStillAccepted() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(204);

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(204);
  }

  @Test
  void blockArtist_withBlankName_returns400() {
    login(uniqueEmail())
      .body(
        """
        {"name": ""}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(400);
  }

  @Test
  void blockArtist_whenUnauthenticated_redirects() {
    unauthenticated()
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/blocked-artists")
      .then()
      .statusCode(302);
  }
}
