package com.example.demo.saved;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

record SaveArtistRequest(@NotBlank String name, @Nullable String genre) {}
