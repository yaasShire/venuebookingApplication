package com.sporton.SportOn.model.venueModel;

import com.sporton.SportOn.entity.Region;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueCreateRequestModel {
    private Long providerId;
    private String name;
    private String address;
    private String city;
    private Long regionId;
    private String description;
    private Long[] facilityIdS;
    private String phoneNumber;
    private String email;
    private String imageURL;
    private Integer numberOfHoursOpen;
    private Double latitude;
    private Double longitude;
    private String openTime;
    private String closeTime;
}