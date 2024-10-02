package com.sporton.SportOn.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponseModel {
    private HttpStatus status;
    private String message;
    private Object data;
}
