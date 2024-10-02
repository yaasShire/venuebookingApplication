package com.sporton.SportOn.service.profileService;

import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.repository.AppUserRepository;
import com.sporton.SportOn.repository.VenueRepository;
import com.sporton.SportOn.service.aswS3Service.AWSS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService{
    private final AppUserRepository appUserRepository;
    private final AWSS3Service awss3Service;
    private final VenueRepository venueRepository;
    @Override
    public CommonResponseModel updateProfileImage(String phoneNumber, MultipartFile image) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isPresent()){
                optionalAppUser.get().setProfileImage(awss3Service.uploadImage(image));
                appUserRepository.save(optionalAppUser.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("User Profile Updated Successfully")
                        .build();
            }else {
                throw new CommonException("No User Found");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel deleteProfileImage(String phoneNumber) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isPresent()){
                awss3Service.deleteImageByUrl(optionalAppUser.get().getProfileImage());
                optionalAppUser.get().setProfileImage(null);
                appUserRepository.save(optionalAppUser.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("User Profile Deleted Successfully")
                        .build();
            }else {
                throw new CommonException("No User Found");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel addOrRemoveFavoriteVenueToUser(String phoneNumber, Long venueId) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) throw new VenueException("User Not Found");
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isEmpty()) throw new VenueException("Venue With Id " + venueId + " Does Not Exist");
if (venueOptional.get().getFavoritedUsers().contains(optionalAppUser.get())){
    venueOptional.get().getFavoritedUsers().remove(optionalAppUser.get());
    venueRepository.save(venueOptional.get());
    return CommonResponseModel.builder()
            .status(HttpStatus.OK)
            .message("Venue Removed From Favorite")
            .build();
}else {
    venueOptional.get().getFavoritedUsers().add(optionalAppUser.get());
    venueRepository.save(venueOptional.get());
    return CommonResponseModel.builder()
            .status(HttpStatus.OK)
            .message("Venue Added To Favorite")
            .build();
}

        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


}
