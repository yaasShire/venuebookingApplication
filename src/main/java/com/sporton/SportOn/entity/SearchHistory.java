package com.sporton.SportOn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String query;
    @ManyToOne
    @NonNull
    private Venue clickedVenue;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    private String deviceId;
}