package com.sporton.SportOn.model.venueModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponseModel {
    private HttpStatus status;
    private String message;
}