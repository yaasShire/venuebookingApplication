package com.sporton.SportOn.exception.venueException;

import com.sporton.SportOn.model.venueModel.VenueExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class VenueExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(VenueException.class)
    public ResponseEntity<VenueExceptionResponse> venueExceptionErrorHandler(VenueException exeption, WebRequest request){
        HttpStatus httpStatus = exeption.getHttpStatus();
        VenueExceptionResponse message = new VenueExceptionResponse(httpStatus, exeption.getMessage());
        return ResponseEntity.status(httpStatus).body(message);
    }
}
