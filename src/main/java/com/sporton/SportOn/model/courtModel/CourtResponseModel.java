package com.sporton.SportOn.model.courtModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourtResponseModel {
    private HttpStatus status;
    private String message;
}
