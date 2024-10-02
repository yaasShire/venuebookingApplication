package com.sporton.SportOn.model.timeSlotModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotRequest {
    private Long courtId;
    private String startTime;
    private String endTime;
    private Boolean available;
    private Double price;
}