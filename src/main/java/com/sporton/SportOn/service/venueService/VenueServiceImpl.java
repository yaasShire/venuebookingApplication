package com.sporton.SportOn.service.venueService;

import com.sporton.SportOn.entity.*;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.venueModel.*;
import com.sporton.SportOn.repository.*;
import com.sporton.SportOn.service.aswS3Service.AWSS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueServiceImpl implements VenueService{
    private final VenueRepository venueRepository;
    private final AppUserRepository appUserRepository;
    private final RegionRepository regionRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final AWSS3Service awss3Service;
    private final FacilityRepository facilityRepository;


    @Override
    public VenueResponseModel createVenue(VenueCreateRequestModel body, String phoneNumber, List<MultipartFile> images) throws VenueException {
        try {
            log.info("here in venue create");
            Optional<Venue> checkVenue = venueRepository.findByName(body.getName());
            if (!checkVenue.isPresent()){
                Optional<AppUser> providerCheck = appUserRepository.findByPhoneNumber(phoneNumber);
                if (!providerCheck.isPresent()) throw new VenueException("There is provider with id " + body.getProviderId());
                if (providerCheck.get().getRole()== Role.PROVIDER || providerCheck.get().getRole()==Role.PROVIDER){
                    log.info("provider info {}", providerCheck.get().getFullName());
                    Optional<Region> optionalRegion = regionRepository.findById(body.getRegionId());
                    if (optionalRegion.isPresent()){
                        log.info("here in venue create");
                        List<String> imageURLS = awss3Service.uploadImages(images);
                        log.info("urls of images--> {}", imageURLS);
                        List<Facility> facilities = facilityRepository.findAllById(List.of(body.getFacilityIdS()));
                        Venue venue = Venue.builder()
                                .name(body.getName())
                                .providerId(providerCheck.get().getId())
                                .phoneNumber(phoneNumber)
                                .city(body.getCity())
                                .address(body.getAddress())
                                .region(optionalRegion.get())
                                .facilities(facilities)
                                .email(body.getEmail())
                                .description(body.getDescription())
                                .numberOfHoursOpen(body.getNumberOfHoursOpen())
                                .latitude(body.getLatitude())
                                .longitude(body.getLongitude())
                                .openTime(body.getOpenTime())
                                .closeTime(body.getCloseTime())
                                .images(imageURLS)
                                .build();
                        venueRepository.save(venue);
                        return VenueResponseModel.builder()
                                .status(HttpStatus.CREATED)
                                .message("Venue Created Successfully")
                                .build();

                    }else {
                        throw new VenueException("There is no region with id " + body.getRegionId(), HttpStatus.BAD_REQUEST);
                    }

                }else {
                    throw new VenueException("Only futsal providers and admins can register futsal");
                }
            }else {
                throw new VenueException("Venue with name '" + body.getName() + "' exists", HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public List<Venue> getAllVenues(int page, int size) throws VenueException {
        try {
            //Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            PageRequest pageRequest = PageRequest.of(page, size);
            Optional<Page<Venue>> optionalVenues = Optional.of(venueRepository.findAll(pageRequest));
            return optionalVenues.get().getContent();
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }
    @Override
    public VenueResponseModel updateVenue(VenueCreateRequestModel body, String phoneNumber, Long venueId, List<MultipartFile> images) throws VenueException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isPresent()){
                if (optionalAppUser.get().getRole()==Role.PROVIDER || optionalAppUser.get().getRole()==Role.ADMIN){
                    Optional<Venue> venueOptional = venueRepository.findById(venueId);
                    if (venueOptional.isPresent()){
                        if (!body.getCity().isEmpty()){
                        venueOptional.get().setCity(body.getCity());
                        }
                        if (!body.getDescription().isEmpty()){
                            venueOptional.get().setDescription(body.getDescription());
                        }
                        if (!body.getEmail().isEmpty()){
                            venueOptional.get().setEmail(body.getEmail());
                        }
                        if (!body.getName().isEmpty()){
                            venueOptional.get().setName(body.getName());
                        }
                        if (!body.getAddress().isEmpty()){
                            venueOptional.get().setAddress(body.getAddress());
                        }
                            venueOptional.get().setAddress(body.getAddress());
                        if (!body.getPhoneNumber().isEmpty()){
                            venueOptional.get().setPhoneNumber(body.getPhoneNumber());
                        }
                        if (body.getOpenTime() !=null){
                            venueOptional.get().setOpenTime(body.getOpenTime());
                        }
                        if (body.getCloseTime() !=null){
                            venueOptional.get().setCloseTime(body.getCloseTime());
                        }
                        if (images !=null){
                            awss3Service.updateVenueImages(venueOptional.get().getId(), images);
                        }
                        List<Facility> facilities = facilityRepository.findAllById(List.of(body.getFacilityIdS()));
                        venueOptional.get().setFacilities(facilities);
                        venueRepository.save(venueOptional.get());
                        return VenueResponseModel.builder()
                                .status(HttpStatus.OK)
                                .message("Venue with id " + venueId + " is updated successfully")
                                .build();
                    }else {
                        throw new VenueException("Venue with id " + venueId + " does not exist");
                    }
                }else {
                    throw new VenueException("Only Venue Providers can update venue");
                }
            }else {
                throw new VenueException("User with id " +optionalAppUser.get().getId() + "does not exist");
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public VenueResponseModel deleteVenue(String phoneNumber, Long venueId) throws VenueException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isPresent()){
                if (optionalAppUser.get().getRole()==Role.PROVIDER || optionalAppUser.get().getRole()==Role.ADMIN){
                    Optional<Venue> optionalVenue = venueRepository.findById(venueId);
                    if (optionalVenue.isPresent()){
                        venueRepository.deleteById(venueId);
                        VenueResponseModel venueResponseModel = VenueResponseModel.builder()
                                .status(HttpStatus.OK)
                                .message("Venue with id " + venueId + " deleted successfully")
                                .build();
                        return venueResponseModel;
                    }else {
                        throw new VenueException("Venue with id " + venueId+  " doest not exist");
                    }
                }else {
                    throw new VenueException("Only Venue Providers Can Delete Venue");
                }
            }else {
                throw new VenueException("User does not exist");
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public Venue getSingleVenue(Long venueId) throws VenueException {
        try {
                    Optional<Venue> optionalVenue = venueRepository.findById(venueId);
                    if (optionalVenue.isPresent()){
                        return optionalVenue.get();
                    }else {
                        throw new VenueException("Venue with id " + venueId+  " doest not exist");
                    }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public List<Venue> getSingleProviderVenues(String phoneNumber, int page, int size) throws VenueException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isPresent()) {
                AppUser user = optionalAppUser.get();
                if (user.getRole() == Role.PROVIDER || user.getRole() == Role.ADMIN) {
                    PageRequest pageRequest = PageRequest.of(page, size);
                    Page<Venue> venuesPage = venueRepository.findByProviderId(user.getId(), pageRequest);
                    if (venuesPage.hasContent()) {
                        return venuesPage.getContent();
                    } else {
                        throw new VenueException("There are no venues for this user", HttpStatus.NOT_FOUND);
                    }
                } else {
                    throw new VenueException("You don't have permission to access this endpoint", HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new VenueException("User does not exist", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new VenueException(e.getMessage());
        }
    }

    private VenueDTO mapToVenueDTO(Venue venue) {
//        log.info("target venue ---> {}", venue.toString());
        String image = venue.getImages().isEmpty() ? null : venue.getImages().get(0);
        return new VenueDTO(
                venue.getId(),
                venue.getProviderId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                venue.getDescription(),
                venue.getPhoneNumber(),
                venue.getEmail(),
                venue.getNumberOfHoursOpen(),
                venue.getLatitude(),
                venue.getLongitude(),
                venue.getNumberOfCourts(),
                venue.getOpenTime(),
                venue.getCloseTime(),
                image,
                venue.getFacilities()
        );
    }

    @Override
    public List<Venue> nearByVenues(int page, int size, NearByVenuesRequestModel body) throws VenueException {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Optional<List<Venue>> optionalVenues = Optional.ofNullable(venueRepository.findVenuesNearUser(body.getLatitude(), body.getLongitude(), pageRequest));
           if (optionalVenues.isPresent()){
            return optionalVenues.get();
           }else {
               throw new VenueException("No Venues Found");
           }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public List<Venue> findPopularVenues(int page, int size) throws VenueException {

        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Optional<List<Venue>> optionalVenues = Optional.ofNullable(venueRepository.findPopularVenues(pageRequest));
            if (optionalVenues.isPresent()){
                return optionalVenues.get();
            }else {
                throw new VenueException("No Venues Found");
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }


    @Override
    public CommonResponseModel saveSearchedVenue(SaveSearchedVenueRequest body) throws VenueException {
        try {
            Optional<Venue> venueOptional = venueRepository.findById(body.getClickedVenueId());
            if (venueOptional.isPresent()){
               Optional<SearchHistory> optionalSearchHistory = searchHistoryRepository.findByQuery(body.getQuery());
               if (optionalSearchHistory.isEmpty()){
                   SearchHistory searchHistory = SearchHistory.builder()
                           .query(body.getQuery())
                           .clickedVenue(venueOptional.get())
                           .deviceId(body.getDeviceId())
                           .timestamp(LocalDateTime.now())
                           .build();
                   searchHistoryRepository.save(searchHistory);
                   return CommonResponseModel.builder()
                           .status(HttpStatus.CREATED)
                           .message("Searched Venue Saved Successfully")
                           .build();
               }
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Venue Save Already")
                        .build();
            }else {
                throw new VenueException("Venue Does Not Exist");
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public List<SearchHistory> getSavedSearchVenues(GetSavedSearchVenuesModel body) throws VenueException {
        try {
            log.info("id {}", body.getDeviceId());
            Optional<List<SearchHistory>> optionalSearchHistories = searchHistoryRepository.findByDeviceId(body.getDeviceId());
            if (optionalSearchHistories.isPresent()){
                return optionalSearchHistories.get();
            }else {
                throw new VenueException("No Recent Searched Venues Found");
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel isVenueFavoritedByUser(String phoneNumber, Long venueId) throws VenueException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) throw new VenueException("User Not Found");
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if (venueOptional.isEmpty()) throw new VenueException("Venue With Id " + venueId + " Does Not Exist");
//                log.info("user favorited it --> {} {}", phoneNumber, venueOptional.get().getName());
            if(venueOptional.get().getFavoritedUsers().contains(optionalAppUser.get())){
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Venue Is Favorited")
                        .build();
            }else {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Venue Is Not Favorited By This User")
                        .build();
            }
        }catch (Exception e){
            throw new VenueException(e.getMessage());

        }
    }

    @Override
    public CommonResponseModel getNumberOfVenues(String phoneNumber) throws VenueException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new VenueException("User not found");
            }

            long venueCount = venueRepository.countByProviderId(optionalAppUser.get().getId());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Number of venues retrieved successfully")
                    .data(venueCount)
                    .build();
        } catch (Exception e) {
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public List<Venue> searchVenuesByNames(List<String> names) {
        return venueRepository.findByNameInIgnoreCase(names);
    }

}
