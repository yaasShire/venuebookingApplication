package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    Optional<SearchHistory> findByQuery(String query);

    Optional<List<SearchHistory>> findByDeviceId(String deviceId);
}
