package com.example.demo.artist;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/liked-artists")
class LikedArtistController {

  private final LikedArtistService likedArtistService;
  private final UserService userService;

  LikedArtistController(
    LikedArtistService likedArtistService,
    UserService userService
  ) {
    this.likedArtistService = likedArtistService;
    this.userService = userService;
  }

  @GetMapping
  List<ArtistResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    return this.likedArtistService.findAll(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ArtistResponse add(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody AddArtistRequest request
  ) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    return this.likedArtistService.add(userId, request.name());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable Long id
  ) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    this.likedArtistService.delete(id, userId);
  }
}
