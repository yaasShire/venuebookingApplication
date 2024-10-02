package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.Rating;
import com.sporton.SportOn.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.venue = :venue")
    Double getAverageRating(@Param("venue") Venue venue);
}