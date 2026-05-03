package com.example.demo.recommendation;

import com.example.demo.artist.LikedArtistService;
import com.example.demo.recommendation.ai.ArtistRecommendationClient;
import com.example.demo.recommendation.ai.RecommendedArtist;
import java.util.List;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class RecommendationService {

  private final RecommendationRepository repository;
  private final LikedArtistService likedArtistService;
  private final ArtistRecommendationClient aiClient;

  RecommendationService(
    RecommendationRepository repository,
    LikedArtistService likedArtistService,
    ArtistRecommendationClient aiClient
  ) {
    this.repository = repository;
    this.likedArtistService = likedArtistService;
    this.aiClient = aiClient;
  }

  List<RecommendationResponse> getAll(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(r ->
        new RecommendationResponse(
          r.getId(),
          r.getName(),
          r.getGenre(),
          r.getReason()
        )
      )
      .toList();
  }

  @Transactional
  public List<RecommendationResponse> generate(Long userId) {
    List<String> names = this.likedArtistService.getNames(userId);
    
    if (names.size() < 3) {
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(422),
        "Need at least 3 liked artists to generate recommendations"
      );
    }

    List<RecommendedArtist> suggested = this.aiClient.recommend(names);
    // Replace previous recommendations so repeated generation does not accumulate rows.
    this.repository.deleteByUserId(userId);
    List<Recommendation> saved = this.repository.saveAll(
      suggested
        .stream()
        .map(r -> new Recommendation(userId, r.name(), r.genre(), r.reason()))
        .toList()
    );

    return saved
      .stream()
      .map(r ->
        new RecommendationResponse(
          r.getId(),
          r.getName(),
          r.getGenre(),
          r.getReason()
        )
      )
      .toList();
  }
}
