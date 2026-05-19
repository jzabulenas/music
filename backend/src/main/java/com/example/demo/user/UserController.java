package com.example.demo.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  MeResponse me(@AuthenticationPrincipal UserDetails userDetails) {
    User user = this.userService.findByEmail(userDetails.getUsername());

    return new MeResponse(user.getId(), user.getEmail());
  }
}
