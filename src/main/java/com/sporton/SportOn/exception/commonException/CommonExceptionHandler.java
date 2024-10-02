package com.sporton.SportOn.exception.commonException;

import com.sporton.SportOn.exception.regionException.RegionException;
import com.sporton.SportOn.model.courtModel.CommonExceptionResponse;
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
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonExceptionResponse> commonExceptionErrorHandler(CommonException exeption, WebRequest request){
        HttpStatus httpStatus = exeption.getHttpStatus();
        CommonExceptionResponse message = new CommonExceptionResponse(httpStatus, exeption.getMessage());
        return ResponseEntity.status(httpStatus).body(message);
    }
}
