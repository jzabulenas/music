package com.example.demo.dismissed;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DismissedArtistService {

  private final DismissedArtistRepository repository;

  DismissedArtistService(DismissedArtistRepository repository) {
    this.repository = repository;
  }

  // Check first instead of relying on catching `DataIntegrityViolationException` alone:
  // when called within an outer transaction (e.g. removing a saved artist), a constraint
  // violation may only surface at commit time, past any catch block here.
  public void dismiss(Long userId, String name) {
    if (this.repository.existsByUserIdAndName(userId, name)) {
      return;
    }

    try {
      this.repository.save(new DismissedArtist(userId, name));
    } catch (DataIntegrityViolationException e) {
      // Already dismissed for this user - dismissing again is not an error, just a no-op.
    }
  }

  public List<String> getNames(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(DismissedArtist::getName)
      .toList();
  }
}
