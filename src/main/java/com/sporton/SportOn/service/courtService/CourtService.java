package com.sporton.SportOn.service.courtService;

import com.sporton.SportOn.entity.Court;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.courtModel.CourtRequestModel;
import com.sporton.SportOn.model.courtModel.CourtResponseModel;

import java.util.List;

public interface CourtService {
    CourtResponseModel createCourt(CourtRequestModel body) throws CommonException;

    Court getCourtById(Long courtId) throws CommonException;

    List<Court> getCourtByVenueId(Long venueId, int page, int size) throws CommonException;

    CourtResponseModel updateCourt(Long courtId, CourtRequestModel body) throws CommonException;

    CourtResponseModel deleteCourt(Long courtId) throws CommonException;
}
