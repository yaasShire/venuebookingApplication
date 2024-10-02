package com.sporton.SportOn.model.bookingModel;

import com.sporton.SportOn.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderOrderResponseDTO {
    private Long venueId;
    private Long orderId;
    private String venueName;
    private String userName;
    private String courtName;
    private LocalDate bookingDate;
    private LocalDate matchDate;
    private Double totalPrice;
    private BookingStatus status;
    private String userPhoneNumber;
    private String startTime;
    private String endTime;
    private String userProfileImage;
}
