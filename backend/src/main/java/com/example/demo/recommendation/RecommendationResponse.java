package com.example.demo.recommendation;

import org.jspecify.annotations.Nullable;

record RecommendationResponse(
  Long id,
  String name,
  @Nullable String genre,
  @Nullable String reason
) {}
