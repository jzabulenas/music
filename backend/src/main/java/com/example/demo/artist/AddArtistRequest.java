package com.example.demo.artist;

import jakarta.validation.constraints.NotBlank;

record AddArtistRequest(@NotBlank String name) {}
