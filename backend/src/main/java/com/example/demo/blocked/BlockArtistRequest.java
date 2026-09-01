package com.example.demo.blocked;

import jakarta.validation.constraints.NotBlank;

record BlockArtistRequest(@NotBlank String name) {}
