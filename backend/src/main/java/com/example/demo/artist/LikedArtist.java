package com.example.demo.artist;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "liked_artists")
class LikedArtist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, updatable = false)
  private Instant addedAt;

  LikedArtist() {}

  LikedArtist(Long userId, String name) {
    this.userId = userId;
    this.name = name;
    this.addedAt = Instant.now();
  }

  Long getId() {
    return this.id;
  }

  Long getUserId() {
    return this.userId;
  }

  String getName() {
    return this.name;
  }

  Instant getAddedAt() {
    return this.addedAt;
  }
}
