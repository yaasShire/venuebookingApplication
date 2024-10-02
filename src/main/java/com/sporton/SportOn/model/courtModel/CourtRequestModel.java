package com.sporton.SportOn.model.courtModel;

import com.sporton.SportOn.entity.CourtSurface;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourtRequestModel {
    private Long venueId;
    private String name;
    private CourtSurface surface;
    private Double width;
    private Double height;
    private Integer activePlayersPerTeam;
    private Double basePrice;
    private String additionalInfo;
}
