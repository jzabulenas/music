package com.example.demo.blocked;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface BlockedArtistRepository extends JpaRepository<BlockedArtist, Long> {
  List<BlockedArtist> findByUserId(Long userId);
}
