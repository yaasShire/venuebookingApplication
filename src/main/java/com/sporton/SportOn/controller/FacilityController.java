package com.sporton.SportOn.controller;

import com.sporton.SportOn.entity.Facility;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.facilityModel.FacilityRequest;
import com.sporton.SportOn.service.facilityService.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facility")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponseModel createFacility(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam("iconUrl") MultipartFile iconUrl
    ) throws CommonException {
        try {
            FacilityRequest body = FacilityRequest.builder()
                    .name(name)
                    .description(description)
                    .iconUrl(iconUrl)
                    .build();
            return facilityService.createFacility(body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/get")
    public List<Facility> getAllFacilities() throws CommonException {
        try {
            return facilityService.getAllFacilities();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/update/{facilityId}")
    public CommonResponseModel updateFacility(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam("iconUrl") MultipartFile iconUrl,
            @PathVariable Long facilityId
    ) throws CommonException {
        try {
            FacilityRequest body = FacilityRequest.builder()
                    .name(name)
                    .description(description)
                    .iconUrl(iconUrl)
                    .build();
           return  facilityService.updateFacility(body, facilityId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{facilityId}")
    public CommonResponseModel updateFacility(
            @PathVariable Long facilityId
    ) throws CommonException {
        try {
            return  facilityService.deleteFacility(facilityId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

}
