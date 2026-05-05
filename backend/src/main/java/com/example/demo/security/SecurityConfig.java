package com.example.demo.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
class SecurityConfig {

  @Value("${app.base-url}")
  private String baseUrl;

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Bean
  SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    MagicLinkSuccessHandler magicLinkSuccessHandler
  ) throws Exception {
    http
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers("/ott/generate", "/login/ott", "/error")
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .oneTimeTokenLogin(ott ->
        ott
          .tokenGenerationSuccessHandler(magicLinkSuccessHandler)
          .successHandler(
            new SimpleUrlAuthenticationSuccessHandler(this.baseUrl + "/app/artists")
          )
      )
      .logout(logout ->
        logout
          .logoutUrl("/logout")
          .logoutSuccessUrl(this.baseUrl + "/login")
          .invalidateHttpSession(true)
          .deleteCookies("SESSION")
      )
      .csrf(csrf ->
        csrf
          .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
          .ignoringRequestMatchers("/ott/generate", "/logout")
      )
      .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
  }

  @Bean
  JdbcOneTimeTokenService oneTimeTokenService(JdbcTemplate jdbcTemplate) {
    return new JdbcOneTimeTokenService(jdbcTemplate);
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(this.allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
