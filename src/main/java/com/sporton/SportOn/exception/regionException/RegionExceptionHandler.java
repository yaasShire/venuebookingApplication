package com.sporton.SportOn.exception.regionException;

import com.sporton.SportOn.model.regionModel.RegionExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class RegionExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(RegionException.class)
    public ResponseEntity<RegionExceptionResponse> venueExceptionErrorHandler(RegionException exeption, WebRequest request){
        HttpStatus httpStatus = exeption.getHttpStatus();
        RegionExceptionResponse message = new RegionExceptionResponse(httpStatus, exeption.getMessage());
        return ResponseEntity.status(httpStatus).body(message);
    }
}