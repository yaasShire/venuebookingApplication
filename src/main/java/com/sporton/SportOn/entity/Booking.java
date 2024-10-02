package com.sporton.SportOn.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private Long userId;
    @NonNull
    private Long providerId;
    @NonNull
    private Long courtId;
    @NonNull
    private Long timeSlotId;
    @NonNull
    private LocalDate bookingDate = LocalDate.now();
    private LocalDate matchDate = LocalDate.now();
    @NonNull
    private Double totalPrice;
    @NonNull
    private BookingStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venue_id")
    private Venue venue;
    private LocalDate recurrenceEndDate; // End date for recurring bookings
//    private LocalTime recurrenceTime; // Time for the booking (e.g., 3:00 PM)
    private String recurrenceTimeSlotId; // Time for the booking (e.g., 3:00 PM - 4:00 PM)


    // New field to specify the day of the week
    @Enumerated(EnumType.STRING)
    private DayOfWeek recurrenceDay; // e.g., MONDAY, TUESDAY, etc.

    // Type of booking
    @Enumerated(EnumType.STRING)
    private BookingType bookingType; // NEW ENUM: ONE_TIME, WEEKLY, MONTHLY

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;


}