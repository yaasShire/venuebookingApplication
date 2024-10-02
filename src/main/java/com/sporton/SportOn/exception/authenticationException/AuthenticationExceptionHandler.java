package com.sporton.SportOn.exception.authenticationException;

import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.authenticationModel.AuthenticationExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class AuthenticationExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthenticationExceptionResponse> authenticationExceptionErrorHandler(AuthenticationException exeption, WebRequest request){
        HttpStatus httpStatus = exeption.getHttpStatus();
        AuthenticationExceptionResponse message = new AuthenticationExceptionResponse(httpStatus, exeption.getMessage());
        return ResponseEntity.status(httpStatus).body(message);
    }
}