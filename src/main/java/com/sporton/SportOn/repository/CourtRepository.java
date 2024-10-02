package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.Court;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Long> {
    Optional<Court> findByName(String name);

    Optional<List<Court>> findByVenueId(Long venueId, PageRequest pageRequest);
}
