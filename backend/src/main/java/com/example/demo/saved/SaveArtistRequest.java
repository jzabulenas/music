package com.example.demo.saved;

import jakarta.validation.constraints.NotBlank;

record SaveArtistRequest(@NotBlank String name, String genre) {}
