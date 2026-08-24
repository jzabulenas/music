package com.example.demo.blocked;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blocked-artists")
class BlockedArtistController {

  private final BlockedArtistService blockedArtistService;
  private final UserService userService;

  BlockedArtistController(
    BlockedArtistService blockedArtistService,
    UserService userService
  ) {
    this.blockedArtistService = blockedArtistService;
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void block(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody BlockArtistRequest request
  ) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    this.blockedArtistService.block(userId, request.name());
  }
}
