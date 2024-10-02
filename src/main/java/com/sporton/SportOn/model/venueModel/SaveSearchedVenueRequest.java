package com.sporton.SportOn.model.venueModel;

import com.sporton.SportOn.entity.Venue;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveSearchedVenueRequest {
    private String query;
    private Long clickedVenueId;
    private String deviceId;
}