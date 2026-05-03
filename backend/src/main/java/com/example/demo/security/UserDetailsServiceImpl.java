package com.example.demo.security;

import com.example.demo.user.User;
import com.example.demo.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class UserDetailsServiceImpl implements UserDetailsService {

  private final UserService userService;

  UserDetailsServiceImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {
    User user = this.userService.findOrCreate(email);

    return org.springframework.security.core.userdetails.User.builder()
      .username(user.getEmail())
      .password("")
      .roles("USER")
      .build();
  }
}
