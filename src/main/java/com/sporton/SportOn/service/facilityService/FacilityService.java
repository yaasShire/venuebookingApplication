package com.sporton.SportOn.service.facilityService;

import com.sporton.SportOn.entity.Facility;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.facilityModel.FacilityRequest;

import java.util.List;

public interface FacilityService {
    CommonResponseModel createFacility(FacilityRequest body) throws CommonException;

    List<Facility> getAllFacilities() throws CommonException;

    CommonResponseModel updateFacility(FacilityRequest body, Long facilityId) throws CommonException;

    CommonResponseModel deleteFacility(Long facilityId) throws CommonException;
}
