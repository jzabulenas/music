package com.example.demo.recommendation;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.BaseIntegrationTest;
import com.example.demo.recommendation.ai.RecommendedArtist;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecommendationIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  void stubAiClient() {
    when(this.recommendationClient.recommend(any())).thenReturn(
      List.of(
        new RecommendedArtist("Artist 1", "Rock", "Reason 1"),
        new RecommendedArtist("Artist 2", "Jazz", "Reason 2"),
        new RecommendedArtist("Artist 3", "Pop", "Reason 3"),
        new RecommendedArtist("Artist 4", "Metal", "Reason 4"),
        new RecommendedArtist("Artist 5", "Blues", "Reason 5")
      )
    );
  }

  @Test
  void getRecommendations_whenEmpty_returnsEmptyList() {
    loginAndGetSpec("user@example.com")
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void generateRecommendations_withThreeOrMoreLikedArtists_returns5Recs() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .body("$", hasSize(5))
      .body("[0].name", notNullValue())
      .body("[0].genre", notNullValue())
      .body("[0].reason", notNullValue());
  }

  @Test
  void getRecommendations_afterGenerate_returnsSameRecs() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    List<Map<String, Object>> generated = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("$");

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("$", equalTo(generated));
  }

  @Test
  void generateRecommendations_twice_replacesOldRecs() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    addLikedArtists(spec, "Radiohead", "Portishead", "Massive Attack");

    spec.post("/api/v1/recommendations/generate");

    when(this.recommendationClient.recommend(any())).thenReturn(
      List.of(
        new RecommendedArtist("New Artist 1", "Rock", "New Reason 1"),
        new RecommendedArtist("New Artist 2", "Jazz", "New Reason 2"),
        new RecommendedArtist("New Artist 3", "Pop", "New Reason 3"),
        new RecommendedArtist("New Artist 4", "Metal", "New Reason 4"),
        new RecommendedArtist("New Artist 5", "Blues", "New Reason 5")
      )
    );

    spec.post("/api/v1/recommendations/generate");

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("name", hasItem("New Artist 1"))
      .body("name", not(hasItem("Artist 1")));
  }

  @Test
  void generateRecommendations_withZeroLikedArtists_returns422() {
    loginAndGetSpec("user@example.com")
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(422);
  }

  @Test
  void generateRecommendations_withOneLikedArtist_returns422() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    addLikedArtists(spec, "Radiohead");

    spec.post("/api/v1/recommendations/generate").then().statusCode(422);
  }

  @Test
  void generateRecommendations_withTwoLikedArtists_returns422() {
    RequestSpecification spec = loginAndGetSpec("user@example.com");
    addLikedArtists(spec, "Radiohead", "Portishead");

    spec.post("/api/v1/recommendations/generate").then().statusCode(422);
  }

  @Test
  void getRecommendations_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .get("/api/v1/recommendations")
      .then()
      .statusCode(equalTo(302));
  }

  @Test
  void generateRecommendations_withoutCsrfToken_returns403() {
    loginAndGetSpecWithoutCsrf("user@example.com")
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(equalTo(403));
  }

  @Test
  void generateRecommendations_whenUnauthenticated_isRejected() {
    unauthenticatedSpec()
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(equalTo(302));
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
        .post("/api/v1/liked-artists");
    }
  }
}
