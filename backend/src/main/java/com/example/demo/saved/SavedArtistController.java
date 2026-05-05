package com.example.demo.saved;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/saved-artists")
class SavedArtistController {

  private final SavedArtistService savedArtistService;
  private final UserService userService;

  SavedArtistController(
    SavedArtistService savedArtistService,
    UserService userService
  ) {
    this.savedArtistService = savedArtistService;
    this.userService = userService;
  }

  @GetMapping
  List<SavedArtistResponse> list(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    return this.savedArtistService.findAll(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  SavedArtistResponse save(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody SaveArtistRequest request
  ) {
    Long userId = this.userService.findByEmail(
      userDetails.getUsername()
    ).getId();

    return this.savedArtistService.save(
      userId,
      request.name(),
      request.genre()
    );
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

    this.savedArtistService.delete(id, userId);
  }
}
