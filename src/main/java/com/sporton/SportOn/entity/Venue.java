package com.sporton.SportOn.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venue")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long providerId;
    @NonNull
    @Column(unique = true)
    private String name;
    @NonNull
    private String address;
    @NonNull
    private String city;
    @NonNull
    private String description;
//    private String[] facilities;
    @NonNull
    private String phoneNumber;
    @NonNull
    private String email;
    @ManyToOne
    @JoinColumn(name = "regionId")
    @JsonBackReference
    private Region region;
    private Integer numberOfHoursOpen;
    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Rating> ratings = new ArrayList<>();
    private int numberOfCourts;
    @NonNull
    private String openTime;
    @NonNull
    private String closeTime;
    @ElementCollection
    @CollectionTable(name = "venue_images", joinColumns = @JoinColumn(name = "venue_id"))
    @Column(name = "image_key")
    private List<String> images = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "venue_facility",
            joinColumns = @JoinColumn(name = "venue_id"),
            inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    private List<Facility> facilities = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "favorited_users",
            joinColumns = @JoinColumn(name = "venue_id"),
            inverseJoinColumns = @JoinColumn(name = "app_userid")
    )
    private List<AppUser> favoritedUsers = new ArrayList<>();

    @JsonBackReference
    @OneToMany(mappedBy = "venue")
    private List<Booking> bookings;

}