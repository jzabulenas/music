package com.example.demo.recommendation;

import org.jspecify.annotations.Nullable;

// A null genre means `Default`: no genre constraint, the original behaviour.
record GenerateRecommendationsRequest(@Nullable String genre) {}
