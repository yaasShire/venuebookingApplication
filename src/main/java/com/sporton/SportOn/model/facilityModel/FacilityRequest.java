package com.sporton.SportOn.model.facilityModel;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityRequest {
    private String name;
    private String description;
    private MultipartFile iconUrl;
}
