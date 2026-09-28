package com.example.demo.user;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

interface GenerationRequestRepository
  extends JpaRepository<GenerationRequest, Long>
{
  long countByUserIdAndCreatedAtGreaterThanEqual(
    Long userId,
    Instant createdAt
  );
}
