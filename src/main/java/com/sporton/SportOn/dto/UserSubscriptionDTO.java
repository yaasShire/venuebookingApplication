package com.sporton.SportOn.dto;

import com.sporton.SportOn.entity.Subscription;
import com.sporton.SportOn.entity.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSubscriptionDTO {
    private String userName;
    private String phoneNumber;
    private Long userId;
    private SubscriptionType subscriptionType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private Boolean subscribed;
}
