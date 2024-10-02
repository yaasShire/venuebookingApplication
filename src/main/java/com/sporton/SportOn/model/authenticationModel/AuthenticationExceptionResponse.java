package com.sporton.SportOn.model.authenticationModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationExceptionResponse {
    private HttpStatus status;
    private String message;
}