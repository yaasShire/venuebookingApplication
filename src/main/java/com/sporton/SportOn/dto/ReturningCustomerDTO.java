package com.sporton.SportOn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturningCustomerDTO {
    private int totalBookings;
    private int returningCustomerBookings;
    private int newCustomerBookings;
    private double returningCustomerPercentage;
    private double newCustomerPercentage;

    // Getters and setters
}
