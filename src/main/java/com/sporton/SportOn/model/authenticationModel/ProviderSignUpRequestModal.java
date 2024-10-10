package com.sporton.SportOn.model.authenticationModel;

import com.sporton.SportOn.entity.MembershipType;
import com.sporton.SportOn.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProviderSignUpRequestModal {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private String gender;
    private String dateOfBirth;
    private String city;
    private Long regionId;
}