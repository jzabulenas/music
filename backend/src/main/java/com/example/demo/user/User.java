package com.example.demo.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  User() {}

  User(String email) {
    this.email = email;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return this.id;
  }

  public String getEmail() {
    return this.email;
  }
}
