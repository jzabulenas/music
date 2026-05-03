package com.example.demo.saved;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class SavedArtistService {

  private final SavedArtistRepository repository;

  SavedArtistService(SavedArtistRepository repository) {
    this.repository = repository;
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

  public SavedArtistResponse save(Long userId, String name, String genre) {
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

  public void delete(Long id, Long userId) {
    this.repository.deleteByIdAndUserId(id, userId);
  }
}
