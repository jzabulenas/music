package com.example.demo.e2e;

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
