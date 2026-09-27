package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.CookieSerializer.CookieValue;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class SlidingSessionCookieFilter extends OncePerRequestFilter {

  private final CookieSerializer cookieSerializer;
  private final int cookieMaxAge;

  SlidingSessionCookieFilter(
    CookieSerializer cookieSerializer,
    @Value("${spring.session.timeout}") Duration sessionTimeout
  ) {
    this.cookieSerializer = cookieSerializer;
    this.cookieMaxAge = (int) sessionTimeout.toSeconds();
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    HttpSession session = request.getSession(false);

    if (session != null && !isLogout(request)) {
      CookieValue cookieValue = new CookieValue(
        request,
        response,
        session.getId()
      );

      cookieValue.setCookieMaxAge(this.cookieMaxAge);
      this.cookieSerializer.writeCookieValue(cookieValue);
    }

    chain.doFilter(request, response);
  }

  private boolean isLogout(HttpServletRequest request) {
    return "/logout".equals(request.getRequestURI());
  }
}
