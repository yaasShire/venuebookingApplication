package com.sporton.SportOn.controller;

import com.sporton.SportOn.configuration.JWTService;
import com.sporton.SportOn.entity.SearchHistory;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.venueModel.*;
import com.sporton.SportOn.service.bookingService.BookingService;
import com.sporton.SportOn.service.timeSlotService.TimeSlotService;
import com.sporton.SportOn.service.venueService.VenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/venue")
@RequiredArgsConstructor
@Slf4j
public class VenueController {
    private final VenueService venueService;
    private final JWTService jwtService;
    private final TimeSlotService timeSlotService;
    private final BookingService bookingService;
    @PostMapping("/create")
    public VenueResponseModel createVenue(
    @RequestParam String name,
    @RequestParam String address,
    @RequestParam String city,
    @RequestParam Long regionId,
    @RequestParam String description,
    @RequestParam Long[] facilityIdS,
    @RequestParam String email,
    @RequestParam Integer numberOfHoursOpen,
    @RequestParam Double latitude,
    @RequestParam Double longitude,
    @RequestParam String openTime,
    @RequestParam String closeTime,
    @RequestHeader("Authorization") String authorizationHeader,
    @RequestParam("images") List<MultipartFile> images
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            VenueCreateRequestModel body = VenueCreateRequestModel.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .city(city)
                    .address(address)
                    .regionId(regionId)
                    .facilityIdS(facilityIdS)
                    .email(email)
                    .description(description)
                    .numberOfHoursOpen(numberOfHoursOpen)
                    .latitude(latitude)
                    .longitude(longitude)
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .build();
            return venueService.createVenue(body, phoneNumber, images);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }
    @GetMapping("/get")
    public List<Venue> getAllVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws VenueException {
        try {
            return venueService.getAllVenues(page, size);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @PutMapping("/update/{venueId}")
    public VenueResponseModel updateVenue(
            @PathVariable Long venueId,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam Long regionId,
            @RequestParam String description,
            @RequestParam Long[] facilityIdS,
            @RequestParam String email,
            @RequestParam Integer numberOfHoursOpen,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String openTime,
            @RequestParam String closeTime,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            VenueCreateRequestModel body = VenueCreateRequestModel.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .city(city)
                    .address(address)
                    .regionId(regionId)
                    .facilityIdS(facilityIdS)
                    .email(email)
                    .description(description)
                    .numberOfHoursOpen(numberOfHoursOpen)
                    .latitude(latitude)
                    .longitude(longitude)
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .build();
            return venueService.updateVenue(body, phoneNumber, venueId, images);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{venueId}")
    public VenueResponseModel deleteVenue(
            @PathVariable Long venueId,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
           return venueService.deleteVenue(phoneNumber, venueId);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/getSingleVenue/{venueId}")
    public Venue getSingleVenue(
            @PathVariable Long venueId
    ) throws VenueException {
        try {
            return venueService.getSingleVenue(venueId);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/myVenues")
    public List<Venue> getSingleProviderVenues(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return venueService.getSingleProviderVenues(phoneNumber, page, size);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @PostMapping("/nearByVenues")
    public List<Venue> nearByVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody NearByVenuesRequestModel body
            ) throws VenueException {
        try {
            return venueService.nearByVenues(page, size, body);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/popularVenues")
    public List<Venue> findPopularVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws VenueException {
        try {
            return venueService.findPopularVenues(page, size);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<Venue>> searchVenues(@RequestParam("names") List<String> names) {
        List<Venue> venues = venueService.searchVenuesByNames(names);
        return ResponseEntity.ok(venues);
    }


    @PostMapping("/saveSearchedVenue")
    public CommonResponseModel saveSearchedVenuesName(@RequestBody SaveSearchedVenueRequest body) throws VenueException {
        return venueService.saveSearchedVenue(body);
    }
    @PostMapping("/getSavedSearchVenues/{venueId}")
    public List<SearchHistory> getSavedSearchVenues(@RequestBody GetSavedSearchVenuesModel body) throws VenueException {
        return venueService.getSavedSearchVenues(body);
    }

    @GetMapping("/isVenueFavoritedByUser/{venueId}")
    public CommonResponseModel isVenueFavoritedByUser(
            @PathVariable Long venueId,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7);
            String phoneNumber = jwtService.extractUsername(token);
            return venueService.isVenueFavoritedByUser(phoneNumber, venueId);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/getNumberOfVenues")
    public CommonResponseModel getNumberOfVenues(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws VenueException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return venueService.getNumberOfVenues(phoneNumber);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/{venueId}/bookings/total")
    public CommonResponseModel getTotalBookings(@PathVariable Long venueId,
                @RequestParam String startDate,
                @RequestParam String endDate) throws CommonException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            return bookingService.getTotalBookings(venueId, start, end);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/{venueId}/occupancy-rate")
    public CommonResponseModel getOccupancyRate(@PathVariable Long venueId,
                                   @RequestParam String startDate,
                                   @RequestParam String endDate) throws CommonException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            return timeSlotService.calculateOccupancyRate(venueId, start, end);
        }catch (Exception e){
            throw new  CommonException(e.getMessage());
        }
    }

    @GetMapping("/{venueId}/revenue")
    public CommonResponseModel getTotalRevenue(@PathVariable Long venueId,
              @RequestParam String startDate,
              @RequestParam String endDate) throws VenueException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            return bookingService.getTotalRevenue(venueId, start, end);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @GetMapping("/{venueId}/popular-time-slots")
    public CommonResponseModel getMostPopularTimeSlots(@PathVariable Long venueId,
              @RequestParam String startDate,
              @RequestParam String endDate,
              @RequestParam(defaultValue = "5") int limit) throws VenueException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            return timeSlotService.getMostPopularTimeSlots(venueId, start, end, limit);
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }

    }


}
