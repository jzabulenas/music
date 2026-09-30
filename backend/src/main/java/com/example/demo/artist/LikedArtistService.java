package com.example.demo.artist;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikedArtistService {

  private final LikedArtistRepository repository;

  LikedArtistService(LikedArtistRepository repository) {
    this.repository = repository;
  }

  public List<ArtistResponse> findAll(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(a -> new ArtistResponse(a.getId(), a.getName(), a.getAddedAt()))
      .toList();
  }

  public ArtistResponse add(Long userId, String name) {
    try {
      LikedArtist artist = this.repository.save(new LikedArtist(userId, name));

      return new ArtistResponse(
        artist.getId(),
        artist.getName(),
        artist.getAddedAt()
      );
    } catch (DataIntegrityViolationException e) {
      // Rely on the DB unique constraint rather than a pre-check query: two concurrent
      // requests could both pass an existsByUserIdAndName check and then both attempt
      // the insert, so we would need to catch this exception either way.
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(409),
        "Artist already added"
      );
    }
  }

  // Check first instead of relying on catching `DataIntegrityViolationException` alone:
  // when called within an outer transaction (e.g. liking a saved artist), a constraint
  // violation may only surface at commit time, past any catch block here.
  // Liking an artist that is already liked is not an error, just a no-op.
  public void addIfAbsent(Long userId, String name) {
    if (this.repository.existsByUserIdAndName(userId, name)) {
      return;
    }

    try {
      this.repository.save(new LikedArtist(userId, name));
    } catch (DataIntegrityViolationException e) {
      // Already liked for this user - liking again is not an error, just a no-op.
    }
  }

  // `deleteByIdAndUserId` uses `em.remove()` internally, which requires an active transaction.
  @Transactional
  public void delete(Long id, Long userId) {
    this.repository.deleteByIdAndUserId(id, userId);
  }

  public List<String> getNames(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(LikedArtist::getName)
      .toList();
  }
}
