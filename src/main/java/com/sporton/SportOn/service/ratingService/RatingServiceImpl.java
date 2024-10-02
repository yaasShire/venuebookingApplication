package com.sporton.SportOn.service.ratingService;

import com.sporton.SportOn.entity.Rating;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.ratingModel.RatingRequestModel;
import com.sporton.SportOn.model.ratingModel.RatingResponseModel;
import com.sporton.SportOn.model.venueModel.VenueResponseModel;
import com.sporton.SportOn.repository.RatingRepository;
import com.sporton.SportOn.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService{
    private final RatingRepository ratingRepository;
    private final VenueRepository venueRepository;
    @Override
    public CommonResponseModel rateVenue(Long venueId, RatingRequestModel body) throws CommonException {
        try {
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isPresent()){
                Rating rate = Rating.builder()
                        .rating(body.getRating())
                        .venue(venueOptional.get())
                        .build();
                ratingRepository.save(rate);
                return CommonResponseModel.builder()
                        .status(HttpStatus.CREATED)
                        .message("Venue  Has Been Rated Successfully")
                        .build();
            }else {
                throw new CommonException("Venue with id " + venueId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public RatingResponseModel getAverageRating(Long venueId) throws CommonException {
        try {
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isPresent()){
              Double ratingAverage = ratingRepository.getAverageRating(venueOptional.get());
                RatingResponseModel ratingResponseModel = RatingResponseModel.builder()
                        .rating(ratingAverage)
                        .build();
                return ratingResponseModel;
            }else {
                throw new CommonException("Venue with id " + venueId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}
