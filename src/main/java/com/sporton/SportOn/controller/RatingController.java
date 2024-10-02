package com.sporton.SportOn.controller;

import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.ratingModel.RatingRequestModel;
import com.sporton.SportOn.model.ratingModel.RatingResponseModel;
import com.sporton.SportOn.service.ratingService.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/venue/{venueId}")
    public CommonResponseModel rateVenue(@PathVariable Long venueId, @RequestBody RatingRequestModel body) throws CommonException {
        try {
            return ratingService.rateVenue(venueId, body);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/venue/{venueId}/average")
    public RatingResponseModel getAverageRating(@PathVariable Long venueId) throws CommonException {
        try {
            return ratingService.getAverageRating(venueId);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
