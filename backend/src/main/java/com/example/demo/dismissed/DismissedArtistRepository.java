package com.example.demo.dismissed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface DismissedArtistRepository
  extends JpaRepository<DismissedArtist, Long>
{
  List<DismissedArtist> findByUserId(Long userId);

  boolean existsByUserIdAndName(Long userId, String name);
}
