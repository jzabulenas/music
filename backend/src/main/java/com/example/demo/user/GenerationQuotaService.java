package com.example.demo.user;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GenerationQuotaService {

  private final GenerationRequestRepository repository;

  @Value("${app.recommendation.daily-limit:5}")
  private int dailyLimit;

  GenerationQuotaService(GenerationRequestRepository repository) {
    this.repository = repository;
  }

  // Admins generate without limits; everyone else gets `dailyLimit` requests per UTC day.
  public void assertWithinDailyLimit(User user) {
    if (user.getRole() == Role.ADMIN) {
      return;
    }

    long usedToday = this.repository.countByUserIdAndCreatedAtGreaterThanEqual(
      user.getId(),
      startOfTodayUtc()
    );

    if (usedToday >= this.dailyLimit) {
      throw new ResponseStatusException(
        HttpStatus.TOO_MANY_REQUESTS,
        "Daily generation limit reached. Try again tomorrow."
      );
    }
  }

  // Returns `null` for admins to signal unlimited requests.
  @Nullable
  Integer remainingToday(User user) {
    if (user.getRole() == Role.ADMIN) {
      return null;
    }

    long usedToday = this.repository.countByUserIdAndCreatedAtGreaterThanEqual(
      user.getId(),
      startOfTodayUtc()
    );

    return Math.max(0, this.dailyLimit - (int) usedToday);
  }

  public void recordGeneration(Long userId) {
    this.repository.save(new GenerationRequest(userId));
  }

  private Instant startOfTodayUtc() {
    return LocalDate.now(ZoneOffset.UTC)
      .atStartOfDay(ZoneOffset.UTC)
      .toInstant();
  }
}
