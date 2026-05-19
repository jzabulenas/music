package com.example.demo.recommendation;

record RecommendationResponse(
  Long id,
  String name,
  String genre,
  String reason
) {}
