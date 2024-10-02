package com.sporton.SportOn.controller;

import com.sporton.SportOn.configuration.JWTService;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.service.profileService.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final ProfileService profileService;
    private final JWTService jwtService;

    @PutMapping("/update/image")
    public CommonResponseModel updateProfileImage(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam MultipartFile image
            ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return profileService.updateProfileImage(phoneNumber, image);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @DeleteMapping("/delete/image")
    public CommonResponseModel deleteProfileImage(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return profileService.deleteProfileImage(phoneNumber);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/addOrRemoveFavoriteVenueToUser/{venueId}")
    public CommonResponseModel addOrRemoveFavoriteVenueToUser(
            @PathVariable Long venueId,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7);
            String phoneNumber = jwtService.extractUsername(token);
            return profileService.addOrRemoveFavoriteVenueToUser(phoneNumber, venueId);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

//    @PutMapping("/checkIfVenueIsFavorited/{venueId}")
//    public CommonResponseModel checkIfVenueIsFavorited(
//            @PathVariable Long venueId,
//            @RequestHeader("Authorization") String authorizationHeader
//    ) throws  CommonException {
//        try {
//            String token = authorizationHeader.substring(7);
//            String phoneNumber = jwtService.extractUsername(token);
//            return profileService.checkIfVenueIsFavorited(phoneNumber, venueId);
//        }catch (Exception e){
//            throw new CommonException(e.getMessage());
//        }
//    }
}
