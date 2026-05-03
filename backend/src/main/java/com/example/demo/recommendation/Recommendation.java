package com.example.demo.recommendation;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recommendations")
class Recommendation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  private String genre;

  @Column(length = 1000)
  private String reason;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  Recommendation() {}

  Recommendation(Long userId, String name, String genre, String reason) {
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

  String getGenre() {
    return this.genre;
  }

  String getReason() {
    return this.reason;
  }
}
