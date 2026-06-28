package com.example.demo.e2e;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class SavedArtistE2ETest extends E2ESupport {

  @Test
  void addArtist_withGenre_thenList_containsArtistWithGenre() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Björk", "genre": "Art Pop"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].name", equalTo("Björk"))
      .body("[0].genre", equalTo("Art Pop"));
  }

  @Test
  void addArtist_withoutGenre_isAccepted() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/saved-artists")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].name", equalTo("Portishead"))
      .body("[0].genre", nullValue());
  }

  @Test
  void addArtist_thenDelete_thenList_isEmpty() {
    RequestSpecification spec = login(uniqueEmail());

    int id = spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .delete("/api/v1/saved-artists/" + id)
      .then()
      .statusCode(204);

    spec.get("/api/v1/saved-artists").then().statusCode(200).body("$", empty());
  }

  @Test
  void addDuplicateArtist_returns409() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"name": "Portishead"}
        """
      )
      .post("/api/v1/saved-artists")
      .then()
      .statusCode(201);

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
}
