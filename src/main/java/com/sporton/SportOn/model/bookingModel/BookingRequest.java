package com.sporton.SportOn.model.bookingModel;

import com.sporton.SportOn.entity.BookingStatus;
import com.sporton.SportOn.entity.BookingType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long courtId;
    private Long providerId;
    private Long timeSlotId;
    private LocalDate matchDate;
    private Double totalPrice;
    private BookingStatus status;
    private int recurrenceFrequency;
    private LocalDate recurrenceEndDate; // End date for recurring bookings
    private String recurrenceTimeSlotId; // Time for the booking (e.g., 3:00 PM - 4:00 PM)

    // New field to specify the day of the week
    @Enumerated(EnumType.STRING)
    private DayOfWeek recurrenceDay; // e.g., MONDAY, TUESDAY, etc.
    private int duration;               // Duration of the recurrence (e.g., 1 month, 1 year)

    // Type of booking
    @Enumerated(EnumType.STRING)
    private BookingType bookingType; // NEW ENUM: ONE_TIME, WEEKLY, MONTHLY
    private boolean isRecurring;
}
