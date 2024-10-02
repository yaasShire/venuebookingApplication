package com.sporton.SportOn.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "court")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long venueId;
    @NonNull
    private String name;
    @NonNull
    private CourtSurface surface;
    @NonNull
    private Double width;
    @NonNull
    private Double height;
    @NonNull
    private Integer activePlayersPerTeam;
    @NonNull
    private Double basePrice;
    private String additionalInfo;
}
