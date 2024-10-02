package com.sporton.SportOn.service.profileService;

import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    CommonResponseModel updateProfileImage(String phoneNumber, MultipartFile image) throws CommonException;

    CommonResponseModel deleteProfileImage(String phoneNumber) throws CommonException;

    CommonResponseModel addOrRemoveFavoriteVenueToUser(String phoneNumber, Long venueId) throws CommonException;

}
