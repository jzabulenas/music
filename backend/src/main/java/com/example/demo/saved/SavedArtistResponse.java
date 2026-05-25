package com.example.demo.saved;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

record SavedArtistResponse(
  Long id,
  String name,
  @Nullable String genre,
  Instant savedAt
) {}
