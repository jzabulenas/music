package com.example.demo.recommendation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
  List<Recommendation> findByUserId(Long userId);

  void deleteByUserId(Long userId);
}
