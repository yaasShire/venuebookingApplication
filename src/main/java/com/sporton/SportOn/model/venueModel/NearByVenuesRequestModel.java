package com.sporton.SportOn.model.venueModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearByVenuesRequestModel {
    private Double latitude;
    private Double longitude;
}
