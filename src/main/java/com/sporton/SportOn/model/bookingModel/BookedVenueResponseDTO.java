package com.sporton.SportOn.model.bookingModel;

import com.sporton.SportOn.entity.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookedVenueResponseDTO {
    private Long id;
    private Long venueId;
    private String venueName;
    private String courtName;
    private LocalDate bookingDate;
    private LocalDate matchDate;
    private Double totalPrice;
    private BookingStatus status;
    private String venuePhoneNumber;
    private String startTime;
    private String endTime;
    private String image;
}
