package com.example.demo.saved;

import java.time.Instant;

record SavedArtistResponse(
  Long id,
  String name,
  String genre,
  Instant savedAt
) {}
