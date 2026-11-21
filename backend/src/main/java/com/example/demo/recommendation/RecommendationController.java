package com.example.demo.recommendation;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/recommendations")
class RecommendationController {

  private final RecommendationService recommendationService;
  private final UserService userService;

  RecommendationController(
    RecommendationService recommendationService,
    UserService userService
  ) {
    this.recommendationService = recommendationService;
    this.userService = userService;
  }

  @GetMapping
  List<RecommendationResponse> list(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.recommendationService.getAll(userId);
  }

  @PostMapping("/generate")
  List<RecommendationResponse> generate(
    @AuthenticationPrincipal UserDetails userDetails,
    @Nullable @RequestBody(
      required = false
    ) GenerateRecommendationsRequest request
  ) {
    User user = this.userService.findByEmail(userDetails.getUsername());

    return this.recommendationService.generate(
      user,
      this.resolveGenre(request)
    );
  }

  // An absent body or genre means `Default`: no genre constraint, the original behaviour.
  // Anything else must match a known genre, otherwise the request is rejected rather than
  // letting an arbitrary string reach the AI prompt.
  private @Nullable RecommendationGenre resolveGenre(
    @Nullable GenerateRecommendationsRequest request
  ) {
    String genre = request == null ? null : request.genre();

    if (genre == null || genre.isBlank()) {
      return null;
    }

    return RecommendationGenre.fromName(genre).orElseThrow(() ->
      new ResponseStatusException(
        HttpStatusCode.valueOf(400),
        "Unknown genre: " + genre
      )
    );
  }
}
