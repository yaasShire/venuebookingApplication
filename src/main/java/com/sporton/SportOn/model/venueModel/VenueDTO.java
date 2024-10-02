package com.sporton.SportOn.model.venueModel;

import com.sporton.SportOn.entity.Facility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
//@AllArgsConstructor
@NoArgsConstructor
//@Builder
public class VenueDTO {
    private Long id;
    private Long providerId;
    private String name;
    private String address;
    private String city;
    private String description;
    private String phoneNumber;
    private String email;
    private int numberOfHoursOpen;
    private double latitude;
    private double longitude;
    private int numberOfCourts;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String image;  // Only one image
    private List<Facility> facilities;

    public VenueDTO(Long id, Long providerId, String name, String address, String city, String description, String phoneNumber, String email, int numberOfHoursOpen, double latitude, double longitude, int numberOfCourts, LocalTime openTime, LocalTime closeTime, List<Facility> facilities, String image) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.numberOfHoursOpen = numberOfHoursOpen;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numberOfCourts = numberOfCourts;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.image = image;
        this.facilities = facilities;
    }

    public VenueDTO(Long id, Long providerId, String name, String address, String city, String description, String phoneNumber, String email, Integer numberOfHoursOpen, Double latitude, Double longitude, int numberOfCourts, String openTime, String closeTime, String image, List<Facility> facilities) {
    }
}