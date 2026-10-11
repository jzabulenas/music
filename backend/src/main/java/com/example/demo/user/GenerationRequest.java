package com.example.demo.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "generation_requests")
class GenerationRequest {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @SuppressWarnings("NullAway.Init")
  GenerationRequest() {}

  GenerationRequest(Long userId) {
    this.userId = userId;
    this.createdAt = Instant.now();
  }

  Long getUserId() {
    return this.userId;
  }
}
