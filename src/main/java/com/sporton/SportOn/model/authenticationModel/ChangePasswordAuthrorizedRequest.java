package com.sporton.SportOn.model.authenticationModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangePasswordAuthrorizedRequest {
    private String oldPassword;
    private String newPassword;
}
