package com.example.demo.artist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface LikedArtistRepository extends JpaRepository<LikedArtist, Long> {
  List<LikedArtist> findByUserId(Long userId);

  boolean existsByUserIdAndName(Long userId, String name);

  void deleteByIdAndUserId(Long id, Long userId);
}
