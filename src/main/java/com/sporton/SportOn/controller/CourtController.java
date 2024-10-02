package com.sporton.SportOn.controller;

import com.sporton.SportOn.entity.Court;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.courtModel.CourtRequestModel;
import com.sporton.SportOn.model.courtModel.CourtResponseModel;
import com.sporton.SportOn.service.courtService.CourtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/court")
@RequiredArgsConstructor
@Slf4j
public class CourtController {
    private final CourtService courtService;

    @PostMapping("/create")
    public CourtResponseModel createCourt( @RequestBody CourtRequestModel body) throws CommonException {
        try {
            return courtService.createCourt(body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getSingleCourt/{courtId}")
    public Court getSingleCourt(@PathVariable Long courtId) throws CommonException {
        try {
            return courtService.getCourtById(courtId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getCourtsByVenueId/{venueId}")
    public List<Court> getCourtsByVenueId(
            @PathVariable Long venueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws CommonException {
        try {
            return courtService.getCourtByVenueId(venueId, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/update/{courtId}")
    public CourtResponseModel updateCourt(
            @PathVariable Long courtId,
            @RequestBody CourtRequestModel body
    ) throws CommonException {
        try {
            log.info("court id {}", courtId);
            return courtService.updateCourt(courtId, body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{courtId}")
    public CourtResponseModel deleteCourt(
            @PathVariable Long courtId
    ) throws CommonException {
        try {
            return courtService.deleteCourt(courtId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}
