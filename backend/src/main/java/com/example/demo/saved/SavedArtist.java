package com.example.demo.saved;

import jakarta.persistence.*;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "saved_artists")
class SavedArtist {

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

  @Column(nullable = false, updatable = false)
  private Instant savedAt;

  @SuppressWarnings("NullAway.Init")
  SavedArtist() {}

  SavedArtist(Long userId, String name, @Nullable String genre) {
    this.userId = userId;
    this.name = name;
    this.genre = genre;
    this.savedAt = Instant.now();
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

  Instant getSavedAt() {
    return this.savedAt;
  }
}
