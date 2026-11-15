package com.example.demo.recommendation;

import java.util.Optional;

// Public so the AI client in the `recommendation.ai` package can render the chosen genre into
// its prompt. The fixed set doubles as the server-side validation vocabulary: only these
// genres ever reach the AI prompt, so arbitrary client strings cannot be injected into it.
public enum RecommendationGenre {
  POP("Pop"),
  RAP("Rap"),
  ROCK("Rock"),
  DANCE("Dance"),
  METAL("Metal"),
  ELECTRONIC("Electronic"),
  R_AND_B("R&B"),
  COUNTRY("Country"),
  JAZZ("Jazz");

  private final String displayName;

  RecommendationGenre(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return this.displayName;
  }

  // Case-insensitive lookup so the exact casing a client sends does not matter.
  public static Optional<RecommendationGenre> fromName(String name) {
    for (RecommendationGenre genre : RecommendationGenre.values()) {
      if (genre.displayName.equalsIgnoreCase(name)) {
        return Optional.of(genre);
      }
    }

    return Optional.empty();
  }
}
