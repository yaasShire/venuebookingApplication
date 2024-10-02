package com.sporton.SportOn.model.authenticationModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SMSTokenRequestModel {
    private String username;
    private String password;
    private String grant_type;
}
