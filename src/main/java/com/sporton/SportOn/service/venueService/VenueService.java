package com.sporton.SportOn.service.venueService;

import com.sporton.SportOn.entity.SearchHistory;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.venueModel.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VenueService {
    VenueResponseModel createVenue(VenueCreateRequestModel body, String phoneNumber, List<MultipartFile> images) throws VenueException;


    List<Venue> getAllVenues(int page, int size) throws VenueException;

    VenueResponseModel updateVenue(VenueCreateRequestModel body, String phoneNumber, Long venueId, List<MultipartFile> images) throws VenueException;

    VenueResponseModel deleteVenue(String phoneNumber, Long venueId) throws VenueException;

    Venue getSingleVenue(Long venueId) throws VenueException;

    List<Venue> getSingleProviderVenues(String phoneNumber, int page, int size) throws VenueException;

    List<Venue> nearByVenues(int page, int size, NearByVenuesRequestModel body) throws VenueException;

    List<Venue> findPopularVenues(int page, int size) throws VenueException;


    CommonResponseModel saveSearchedVenue(SaveSearchedVenueRequest body) throws VenueException;

    List<SearchHistory> getSavedSearchVenues(GetSavedSearchVenuesModel body) throws VenueException;

    CommonResponseModel isVenueFavoritedByUser(String phoneNumber, Long venueId) throws VenueException;

    CommonResponseModel getNumberOfVenues(String phoneNumber) throws VenueException;

    List<Venue> searchVenuesByNames(List<String> names);
}
