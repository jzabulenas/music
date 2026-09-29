package com.example.demo.recommendation;

import com.example.demo.artist.LikedArtistService;
import com.example.demo.blocked.BlockedArtistService;
import com.example.demo.dismissed.DismissedArtistService;
import com.example.demo.recommendation.ai.ArtistRecommendationClient;
import com.example.demo.recommendation.ai.RecommendedArtist;
import com.example.demo.saved.SavedArtistService;
import com.example.demo.user.GenerationQuotaService;
import com.example.demo.user.User;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class RecommendationService {

  private final RecommendationRepository repository;
  private final LikedArtistService likedArtistService;
  private final BlockedArtistService blockedArtistService;
  private final DismissedArtistService dismissedArtistService;
  private final SavedArtistService savedArtistService;
  private final ArtistRecommendationClient aiClient;
  private final GenerationQuotaService quotaService;

  RecommendationService(
    RecommendationRepository repository,
    LikedArtistService likedArtistService,
    BlockedArtistService blockedArtistService,
    DismissedArtistService dismissedArtistService,
    SavedArtistService savedArtistService,
    ArtistRecommendationClient aiClient,
    GenerationQuotaService quotaService
  ) {
    this.repository = repository;
    this.likedArtistService = likedArtistService;
    this.blockedArtistService = blockedArtistService;
    this.dismissedArtistService = dismissedArtistService;
    this.savedArtistService = savedArtistService;
    this.aiClient = aiClient;
    this.quotaService = quotaService;
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
  public List<RecommendationResponse> generate(User user) {
    Long userId = user.getId();
    List<String> names = this.likedArtistService.getNames(userId);

    if (names.size() < 3) {
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(422),
        "Need at least 3 liked artists to generate recommendations"
      );
    }

    // Enforce the daily quota before spending an AI call.
    this.quotaService.assertWithinDailyLimit(user);

    List<String> blockedNames = this.blockedArtistService.getNames(userId);
    List<String> dismissedNames = this.dismissedArtistService.getNames(userId);
    List<String> savedNames = this.savedArtistService.getNames(userId);

    // Blocked and dismissed artists mean the same thing to the model: already rejected.
    List<String> rejectedNames = Stream.concat(
      blockedNames.stream(),
      dismissedNames.stream()
    ).toList();

    List<RecommendedArtist> suggested = this.aiClient.recommend(
      names,
      rejectedNames,
      savedNames
    );

    Set<String> excludedLower = Stream.of(
      blockedNames,
      dismissedNames,
      savedNames
    )
      .flatMap(List::stream)
      .map(n -> n.toLowerCase(Locale.ROOT))
      .collect(Collectors.toSet());

    // Defensive safety net: the prompt already asks the model to avoid rejected or saved artists,
    // but drop any it suggests anyway rather than reintroduce something already rejected or saved.
    List<RecommendedArtist> filtered = suggested
      .stream()
      .filter(r -> !excludedLower.contains(r.name().toLowerCase(Locale.ROOT)))
      .toList();

    // Replace previous recommendations so repeated generation does not accumulate rows.
    this.repository.deleteByUserId(userId);
    List<Recommendation> saved = this.repository.saveAll(
      filtered
        .stream()
        .map(r -> new Recommendation(userId, r.name(), r.genre(), r.reason()))
        .toList()
    );

    // Record the successful generation so it counts against the daily quota.
    this.quotaService.recordGeneration(userId);

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
