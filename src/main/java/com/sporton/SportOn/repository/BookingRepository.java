package com.sporton.SportOn.repository;

import com.sporton.SportOn.dto.MonthlyIncome;
import com.sporton.SportOn.entity.Booking;
import com.sporton.SportOn.entity.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<List<Booking>> findByUserId(Pageable pageable, Long userId);

    Optional<List<Booking>> findByProviderId(Pageable pageable, Long id);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = :status AND b.bookingDate BETWEEN :startDate AND :endDate")
    Double findTotalIncomeByDateRange(@Param("status") BookingStatus status, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(b.matchDate) AS year, MONTH(b.matchDate) AS month, SUM(b.totalPrice) AS totalIncome " +
            "FROM Booking b " +
            "WHERE b.matchDate >= :startDate " +
            "GROUP BY YEAR(b.matchDate), MONTH(b.matchDate) " +
            "ORDER BY YEAR(b.matchDate), MONTH(b.matchDate)")
    List<MonthlyIncome> findMonthlyIncome(@Param("startDate") LocalDate startDate);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.providerId = :providerId AND b.status = :status")
    Double findTotalIncomeForProvider(@Param("providerId") Long providerId, @Param("status") BookingStatus status);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.venue.id = :venueId AND b.status = com.sporton.SportOn.entity.BookingStatus.Confirmed")
    Double findTotalIncomeForVenue(@Param("venueId") Long venueId);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.venue.id = :venueId AND b.status = com.sporton.SportOn.entity.BookingStatus.Confirmed AND b.bookingDate BETWEEN :startDate AND :endDate")
    Double findTotalIncomeForVenueInPeriod(@Param("venueId") Long venueId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.venue.id = :venueId AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsByVenueAndPeriod(@Param("venueId") Long venueId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    // Query to calculate total revenue for a venue over a specified date range
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.venue.id = :venueId AND b.bookingDate BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueByVenueAndDate(@Param("venueId") Long venueId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    List<Booking> findByVenueId(Long venueId);

    @Query("SELECT b FROM Booking b WHERE b.paymentDueDate < :currentDate AND b.status = :status")
    List<Booking> findOverdueBookings(@Param("currentDate") LocalDate currentDate, @Param("status") BookingStatus status);


    @Query("SELECT b FROM Booking b WHERE b.paymentDueDate = :dueDate")
    List<Booking> findBookingsWithUpcomingPayments(@Param("dueDate") LocalDate dueDate);

}
