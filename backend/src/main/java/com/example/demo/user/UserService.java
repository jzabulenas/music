package com.example.demo.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository repository;

  UserService(UserRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public User findOrCreate(String email) {
    return this.repository
      .findByEmail(email)
      .orElseGet(() -> this.repository.save(new User(email)));
  }

  public User findByEmail(String email) {
    return this.repository
      .findByEmail(email)
      .orElseThrow(() ->
        new IllegalArgumentException("User not found: " + email)
      );
  }
}
