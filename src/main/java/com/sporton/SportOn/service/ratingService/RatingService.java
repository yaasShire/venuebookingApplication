package com.sporton.SportOn.service.ratingService;

import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.ratingModel.RatingRequestModel;
import com.sporton.SportOn.model.ratingModel.RatingResponseModel;

public interface RatingService {
    CommonResponseModel rateVenue(Long venueId, RatingRequestModel body) throws CommonException;

    RatingResponseModel getAverageRating(Long venueId) throws CommonException;
}
