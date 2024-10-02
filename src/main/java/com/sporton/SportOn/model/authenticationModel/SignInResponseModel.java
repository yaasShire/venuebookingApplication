package com.sporton.SportOn.model.authenticationModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SignInResponseModel {
    private HttpStatus status;
    private String message;
    private Long id;
    private String phoneNumber;
    private String joinedDate;
    private Boolean isSubscribed;
    private Boolean authenticated;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String profileImage;
}