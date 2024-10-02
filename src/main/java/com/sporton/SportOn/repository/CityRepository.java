package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByName(String cityName);

    @Query("SELECT c FROM City c WHERE c.region.id = :regionId")
    List<City> findAllByRegionId(@Param("regionId") Long regionId);
}
