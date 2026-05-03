package com.example.demo.saved;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SavedArtistRepository extends JpaRepository<SavedArtist, Long> {
  List<SavedArtist> findByUserId(Long userId);

  void deleteByIdAndUserId(Long id, Long userId);
}
