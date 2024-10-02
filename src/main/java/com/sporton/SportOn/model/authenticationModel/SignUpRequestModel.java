package com.sporton.SportOn.model.authenticationModel;

import com.sporton.SportOn.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SignUpRequestModel {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private Role role=Role.USER;
}
