package com.example.demo.artist;

import java.time.Instant;

record ArtistResponse(Long id, String name, Instant addedAt) {}
