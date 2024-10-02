package com.sporton.SportOn.model.authenticationModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UpdateProfileData {
    private String fullName;
    private String email;
    private String password;
}
