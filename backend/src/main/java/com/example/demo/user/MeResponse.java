package com.example.demo.user;

import org.jspecify.annotations.Nullable;

record MeResponse(
  Long id,
  String email,
  Role role,
  @Nullable Integer remainingRequests
) {}
