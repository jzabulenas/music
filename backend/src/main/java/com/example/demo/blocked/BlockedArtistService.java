package com.example.demo.blocked;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class BlockedArtistService {

  private final BlockedArtistRepository repository;

  BlockedArtistService(BlockedArtistRepository repository) {
    this.repository = repository;
  }

  public void block(Long userId, String name) {
    try {
      this.repository.save(new BlockedArtist(userId, name));
    } catch (DataIntegrityViolationException e) {
      // Already blocked for this user - blocking again is not an error, just a no-op.
    }
  }

  public List<String> getNames(Long userId) {
    return this.repository.findByUserId(userId)
      .stream()
      .map(BlockedArtist::getName)
      .toList();
  }
}
