package com.example.demo.saved;

import com.example.demo.dismissed.DismissedArtistService;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SavedArtistService {

  private final SavedArtistRepository repository;
  private final DismissedArtistService dismissedArtistService;

  SavedArtistService(
    SavedArtistRepository repository,
    DismissedArtistService dismissedArtistService
  ) {
    this.repository = repository;
    this.dismissedArtistService = dismissedArtistService;
  }

  public List<SavedArtistResponse> findAll(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(a ->
        new SavedArtistResponse(
          a.getId(),
          a.getName(),
          a.getGenre(),
          a.getSavedAt()
        )
      )
      .toList();
  }

  public SavedArtistResponse save(
    Long userId,
    String name,
    @Nullable String genre
  ) {
    try {
      SavedArtist artist = this.repository.save(
        new SavedArtist(userId, name, genre)
      );

      return new SavedArtistResponse(
        artist.getId(),
        artist.getName(),
        artist.getGenre(),
        artist.getSavedAt()
      );
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(409),
        "Artist already saved"
      );
    }
  }

  // `deleteByIdAndUserId` uses `em.remove()` internally, which requires an active transaction.
  // Removing an artist also dismisses it, so it is never suggested in future generations.
  @Transactional
  public void delete(Long id, Long userId) {
    Optional<SavedArtist> artist = this.repository.findByIdAndUserId(
      id,
      userId
    );

    // Defensive: an unknown id, or one owned by another user, is a no-op rather than an error.
    if (artist.isEmpty()) {
      return;
    }

    this.dismissedArtistService.dismiss(userId, artist.get().getName());

    this.repository.deleteByIdAndUserId(id, userId);
  }

  public List<String> getNames(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(SavedArtist::getName)
      .toList();
  }
}
