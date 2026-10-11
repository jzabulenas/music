package com.example.demo.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.USER;

  @SuppressWarnings("NullAway.Init")
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

  Role getRole() {
    return this.role;
  }
}
