package com.sporton.SportOn.repository;

import com.sporton.SportOn.entity.TimeSlot;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Query(value = "SELECT * FROM time_slot WHERE court_id = :courtId AND (" +
            "TO_TIMESTAMP(start_time, 'HH12:MI AM') < TO_TIMESTAMP(:endTime, 'HH12:MI AM') AND " +
            "TO_TIMESTAMP(end_time, 'HH12:MI AM') > TO_TIMESTAMP(:startTime, 'HH12:MI AM')" +
            ")", nativeQuery = true)
    Optional<List<TimeSlot>> findByMatchingOrOverlappingTimeSlotsForCourt(
            String startTime,
            String endTime,
            Long courtId
    );

    Optional<List<TimeSlot>> findByCourtId(Long courtId, PageRequest pageRequest);

    // Query to get all time slots for a specific venue and date range
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.courtId IN (SELECT c.id FROM Court c WHERE c.venueId = :venueId)")
    List<TimeSlot> findTimeSlotsByVenue(@Param("venueId") Long venueId);

    // Query to count the number of bookings for each time slot at a venue
    @Query("SELECT ts, COUNT(b) FROM TimeSlot ts LEFT JOIN Booking b ON ts.id = b.timeSlotId " +
            "WHERE ts.courtId IN (SELECT c.id FROM Court c WHERE c.venueId = :venueId) " +
            "AND (b.matchDate BETWEEN :startDate AND :endDate OR b.matchDate IS NULL) " +
            "GROUP BY ts ORDER BY COUNT(b) DESC")
    List<Object[]> findPopularTimeSlotsByVenue(@Param("venueId") Long venueId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

}
