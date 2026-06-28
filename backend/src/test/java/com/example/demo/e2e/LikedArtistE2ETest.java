package com.example.demo.e2e;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class LikedArtistE2ETest extends E2ESupport {

  @Test
  void addArtist_thenList_containsNewArtist() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("name", hasItem("Radiohead"));
  }

  @Test
  void addArtist_thenDelete_thenList_isEmpty() {
    RequestSpecification spec = login(uniqueEmail());

    int id = addArtist(spec, "Radiohead");

    spec
      .delete("/api/v1/liked-artists/" + id)
      .then()
      .statusCode(204);

    spec.get("/api/v1/liked-artists").then().statusCode(200).body("$", empty());
  }

  @Test
  void addDuplicateArtist_returns409() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Radiohead"}
        """
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(201);

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
  void addArtistWithBlankName_returns400() {
    login(uniqueEmail())
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
  void likedArtists_areIsolatedBetweenUsers() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    addArtist(userA, "Portishead");
    addArtist(userB, "Radiohead");

    userA
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("name", hasItem("Portishead"))
      .body("name", not(hasItem("Radiohead")));

    userB
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("name", hasItem("Radiohead"))
      .body("name", not(hasItem("Portishead")));
  }

  @Test
  void deleteArtist_belongingToOtherUser_doesNotDeleteIt() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    int id = addArtist(userA, "Radiohead");

    userB
      .delete("/api/v1/liked-artists/" + id)
      .then()
      .statusCode(204);

    userA
      .get("/api/v1/liked-artists")
      .then()
      .statusCode(200)
      .body("name", hasItem("Radiohead"));
  }

  @Test
  void likedArtists_whenUnauthenticated_redirects() {
    unauthenticated().get("/api/v1/liked-artists").then().statusCode(302);
  }

  private static int addArtist(RequestSpecification spec, String name) {
    return spec
      .body(
        """
        {"name": "%s"}
        """.formatted(name)
      )
      .post("/api/v1/liked-artists")
      .then()
      .statusCode(201)
      .extract()
      .path("id");
  }
}
