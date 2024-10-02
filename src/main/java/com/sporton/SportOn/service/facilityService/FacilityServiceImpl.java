package com.sporton.SportOn.service.facilityService;

import com.sporton.SportOn.entity.Facility;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.facilityModel.FacilityRequest;
import com.sporton.SportOn.repository.FacilityRepository;
import com.sporton.SportOn.service.aswS3Service.AWSS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService{
    private final FacilityRepository facilityRepository;
    private final AWSS3Service awss3Service;
    @Override
    public CommonResponseModel createFacility(FacilityRequest body) throws CommonException {
        try {
            Optional<Facility> optionalFacility = facilityRepository.findByName(body.getName());
            if (optionalFacility.isPresent()) throw new CommonException("Facility With Name " + body.getName() + " Already Exists");
            Facility facility = Facility.builder()
                    .name(body.getName())
                    .description(body.getDescription())
                    .iconUrl(awss3Service.uploadImage(body.getIconUrl()))
                    .build();
            facilityRepository.save(facility);
            return CommonResponseModel.builder()
                    .status(HttpStatus.CREATED)
                    .message("Facility Created Successfully")
                    .build();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<Facility> getAllFacilities() throws CommonException {
        try {
            return facilityRepository.findAll();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel updateFacility(FacilityRequest body, Long facilityId) throws CommonException {
        try {
            Optional<Facility> optionalFacility = facilityRepository.findById(facilityId);
            if (optionalFacility.isPresent()){
                optionalFacility.get().setName(body.getName());
                optionalFacility.get().setDescription(body.getDescription());
                optionalFacility.get().setIconUrl(awss3Service.updateImage(optionalFacility.get().getIconUrl(), body.getIconUrl()));
                facilityRepository.save(optionalFacility.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.CREATED)
                        .message("Facility Updated Successfully")
                        .build();
            }else {
                throw new CommonException("Facility With Id " + facilityId + " Does Not Exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel deleteFacility(Long facilityId) throws CommonException {
        try {
            Optional<Facility> optionalFacility = facilityRepository.findById(facilityId);
            if (optionalFacility.isPresent()){
//                awss3Service.deleteImageByUrl(optionalFacility.get().getIconUrl());
                facilityRepository.deleteById(facilityId);
                return CommonResponseModel.builder()
                        .status(HttpStatus.CREATED)
                        .message("Facility Deleted Successfully")
                        .build();
            }else {
                throw new CommonException("Facility With Id " + facilityId + " Does Not Exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }
}