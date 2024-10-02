package com.sporton.SportOn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileDTO {
    private String fullName;
    private String email;
    private String city;
    private String gender;
    private Integer age;
    private String ageGroup;
}
