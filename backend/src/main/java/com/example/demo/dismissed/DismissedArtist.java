package com.example.demo.dismissed;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dismissed_artists")
class DismissedArtist {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, updatable = false)
  private Instant dismissedAt;

  @SuppressWarnings("NullAway.Init")
  DismissedArtist() {}

  DismissedArtist(Long userId, String name) {
    this.userId = userId;
    this.name = name;
    this.dismissedAt = Instant.now();
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

  Instant getDismissedAt() {
    return this.dismissedAt;
  }
}
