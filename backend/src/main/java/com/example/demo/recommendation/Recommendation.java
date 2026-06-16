package com.example.demo.recommendation;

import jakarta.persistence.*;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recommendations")
class Recommendation {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Nullable
  private String genre;

  @Column(length = 1000)
  @Nullable
  private String reason;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @SuppressWarnings("NullAway.Init")
  Recommendation() {}

  Recommendation(
    Long userId,
    String name,
    @Nullable String genre,
    @Nullable String reason
  ) {
    this.userId = userId;
    this.name = name;
    this.genre = genre;
    this.reason = reason;
    this.createdAt = Instant.now();
  }

  Long getId() {
    return this.id;
  }

  String getName() {
    return this.name;
  }

  @Nullable
  String getGenre() {
    return this.genre;
  }

  @Nullable
  String getReason() {
    return this.reason;
  }
}
