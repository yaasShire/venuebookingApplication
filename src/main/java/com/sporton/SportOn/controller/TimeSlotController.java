package com.sporton.SportOn.controller;

import com.sporton.SportOn.entity.TimeSlot;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.timeSlotModel.TimeSlotRequest;
import com.sporton.SportOn.service.timeSlotService.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timeslot")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;
    @PostMapping("/create")
    public CommonResponseModel createTimeSlot(@RequestBody TimeSlotRequest body) throws CommonException {
        try {
            return timeSlotService.createTimeSlot(body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
    @GetMapping("/getTimeSlotByCourtId/{courtId}")
    public List<TimeSlot> getTimeSlotsByCourtId(
            @PathVariable Long courtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws CommonException {
        try {
            return timeSlotService.getTimeSlotsByCourtId(courtId, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/update/{timeSlotId}")
    public CommonResponseModel updateTimeSlot(
            @RequestBody TimeSlotRequest body,
            @PathVariable Long timeSlotId
    ) throws CommonException {
        try {
            return timeSlotService.updateTimeSlot(timeSlotId, body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{timeSlotId}")
    public CommonResponseModel deleteTimeSlot(@PathVariable Long timeSlotId) throws CommonException {
        try {
            return timeSlotService.deleteTimeSlot(timeSlotId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}
