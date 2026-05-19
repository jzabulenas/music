package com.example.demo.recommendation.ai;

import org.jspecify.annotations.Nullable;

public record RecommendedArtist(
  String name,
  @Nullable String genre,
  @Nullable String reason
) {}
