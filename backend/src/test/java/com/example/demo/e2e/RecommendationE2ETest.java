package com.example.demo.e2e;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class RecommendationE2ETest extends E2ESupport {

  @Test
  void generateRecommendations_withThreeLikedArtists_returnsExactlyFiveArtists() {
    RequestSpecification spec = login(uniqueEmail());
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .body("$", hasSize(5))
      .body("name", everyItem(not(emptyOrNullString())))
      .body("genre", everyItem(anyOf(nullValue(), not(emptyString()))))
      .body("reason", everyItem(not(emptyOrNullString())));
  }

  @Test
  void getRecommendations_afterGenerate_returnsSameArtists() {
    RequestSpecification spec = login(uniqueEmail());
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    List<String> generatedNames = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("name", String.class);

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("name", equalTo(generatedNames));
  }

  @Test
  void generateTwice_replacesOldRecommendations() {
    RequestSpecification spec = login(uniqueEmail());
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    List<Integer> firstIds = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("id", Integer.class);

    List<Integer> secondIds = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("id", Integer.class);

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("id", equalTo(secondIds))
      .body("id", not(equalTo(firstIds)));
  }

  @Test
  void generate_withFewerThanThreeLikedArtists_returns422() {
    RequestSpecification spec = login(uniqueEmail());
    addLikedArtists(spec, "Radiohead", "Portishead");

    spec.post("/api/v1/recommendations/generate").then().statusCode(422);
  }

  @Test
  void recommendations_whenUnauthenticated_redirects() {
    unauthenticated()
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(302);
  }

  private static void addLikedArtists(
    RequestSpecification spec,
    String... names
  ) {
    for (String name : names) {
      spec
        .body(
          """
          {"name": "%s"}
          """.formatted(name)
        )
        .post("/api/v1/liked-artists")
        .then()
        .statusCode(201);
    }
  }
}
