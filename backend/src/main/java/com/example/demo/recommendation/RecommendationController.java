package com.example.demo.recommendation;

import com.example.demo.user.UserService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    Long userId = this.userService.findByEmail(userDetails.getUsername()).getId();

    return this.recommendationService.getAll(userId);
  }

  @PostMapping("/generate")
  List<RecommendationResponse> generate(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    Long userId = this.userService.findByEmail(userDetails.getUsername()).getId();

    return this.recommendationService.generate(userId);
  }
}
