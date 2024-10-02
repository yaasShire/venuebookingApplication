package com.sporton.SportOn.service.timeSlotService;

import com.sporton.SportOn.entity.Court;
import com.sporton.SportOn.entity.TimeSlot;
import com.sporton.SportOn.entity.Venue;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.timeSlotModel.TimeSlotRequest;
import com.sporton.SportOn.repository.CourtRepository;
import com.sporton.SportOn.repository.TimeSlotRepository;
import com.sporton.SportOn.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService{
    private final TimeSlotRepository timeSlotRepository;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    @Override
    public CommonResponseModel createTimeSlot(TimeSlotRequest body) throws CommonException {
        try {
            // Fetch the court details
            Optional<Court> optionalCourt = courtRepository.findById(body.getCourtId());
            if (optionalCourt.isPresent()) {
                // Fetch the venue details using the venueId from the court
                Optional<Venue> optionalVenue = venueRepository.findById(optionalCourt.get().getVenueId());
                if (optionalVenue.isPresent()) {
                    Venue venue = optionalVenue.get();

                    // Parse the venue's openTime and closeTime into LocalTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a"); // Update the formatter
                    LocalTime openTime = LocalTime.parse(venue.getOpenTime(), formatter);
                    LocalTime closeTime = LocalTime.parse(venue.getCloseTime(), formatter);

                    // Parse the time slot's startTime and endTime from the request body
                    LocalTime startTime = LocalTime.parse(body.getStartTime(), formatter);
                    LocalTime endTime = LocalTime.parse(body.getEndTime(), formatter);

                    // Check if the time slot is within the venue's operating hours
                    if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                        throw new CommonException("Time Slot is outside of the venue's operating hours.");
                    }

                    // Check if there are any existing time slots that overlap with the new time slot
                    Optional<List<TimeSlot>> optionalTimeSlot = timeSlotRepository.findByMatchingOrOverlappingTimeSlotsForCourt(body.getStartTime(), body.getEndTime(), body.getCourtId());
                    log.info("value --> {}", optionalTimeSlot.get());

                    if (optionalTimeSlot.get().isEmpty()) {
                        // Create the new time slot if there is no overlap
                        TimeSlot timeSlot = TimeSlot.builder()
                                .courtId(body.getCourtId())
                                .startTime(body.getStartTime())
                                .endTime(body.getEndTime())
                                .available(true)
                                .price(body.getPrice())
                                .build();
                        timeSlotRepository.save(timeSlot);

                        return CommonResponseModel.builder()
                                .status(HttpStatus.CREATED)
                                .message("Time Slot Created Successfully")
                                .build();
                    } else {
                        log.info("There is time slot matching this time slot");
                        throw new CommonException("Invalid Time Slot, Time Slot Already Exists");
                    }
                } else {
                    throw new CommonException("Venue with id " + optionalCourt.get().getVenueId() + " does not exist");
                }
            } else {
                throw new CommonException("Court with id " + body.getCourtId() + " does not exist");
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }


    @Override
    public List<TimeSlot> getTimeSlotsByCourtId(Long courtId, int page, int size) throws CommonException {
        try {
            Optional<Court> optionalCourt = courtRepository.findById(courtId);
            if (optionalCourt.isPresent()){
                PageRequest pageRequest = PageRequest.of(page, size);
                Optional<List<TimeSlot>> optionalTimeSlots = timeSlotRepository.findByCourtId(courtId, pageRequest);
                if (optionalTimeSlots.isPresent()){
                    return optionalTimeSlots.get();
                }else {
                    throw new CommonException("No Court is Found");
                }
            }else {
                throw new CommonException("Court with id " + courtId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel updateTimeSlot(Long timeSlotId, TimeSlotRequest body) throws CommonException {
        try {
            Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(timeSlotId);
            if (optionalTimeSlot.isPresent()){
                Optional<List<TimeSlot>> checkTimeSlotExist = timeSlotRepository.findByMatchingOrOverlappingTimeSlotsForCourt(body.getStartTime(), body.getEndTime(), body.getCourtId());
                if (checkTimeSlotExist.isPresent() && checkTimeSlotExist.get().isEmpty()) {
                    TimeSlot timeSlotToUpdate = optionalTimeSlot.get();
                    timeSlotToUpdate.setStartTime(body.getStartTime());
                    timeSlotToUpdate.setEndTime(body.getEndTime());
                    timeSlotToUpdate.setAvailable(body.getAvailable());
                    timeSlotToUpdate.setPrice(body.getPrice());
                    timeSlotRepository.save(timeSlotToUpdate);

                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("Time Slot Updated Successfully")
                            .build();
                } else {
                    throw new CommonException("Invalid Time Slot, Time Slot Already Exists");
                }
            }else {
                throw new CommonException("Time Slot with id " + timeSlotId + " does not exist");
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel deleteTimeSlot(Long timeSlotId) throws CommonException {
        Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(timeSlotId);
        if (optionalTimeSlot.isPresent()){
            timeSlotRepository.deleteById(timeSlotId);
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Time Slot Deleted Successfully")
                    .build();
        }else {
            throw new CommonException("Time Slot with id " + timeSlotId + " does not exist");
        }
    }

    @Override
    public CommonResponseModel calculateOccupancyRate(Long venueId, LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> timeSlots = timeSlotRepository.findTimeSlotsByVenue(venueId);
        int totalSlots = 0;
        int bookedSlots = 0;

        for (TimeSlot timeSlot : timeSlots) {
            totalSlots++;  // Count total time slots

            // Check if the time slot was booked on any date within the given period
            for (LocalDate bookedDate : timeSlot.getBookedDates()) {
                if (!bookedDate.isBefore(startDate) && !bookedDate.isAfter(endDate)) {
                    bookedSlots++;
                    break;
                }
            }
        }

        if (totalSlots == 0) {
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("occupancy rate")
                    .data(0.0)
                    .build();
        }

        // Calculate the percentage of booked time slots
        return CommonResponseModel.builder()
                .status(HttpStatus.OK)
                .message("occupancy rate")
                .data((double) bookedSlots / totalSlots * 100)
                .build();
    }

    @Override
    public CommonResponseModel getMostPopularTimeSlots(Long venueId, LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> results = timeSlotRepository.findPopularTimeSlotsByVenue(venueId, startDate, endDate);

        // Extract TimeSlot objects from the query results and limit the output
        return CommonResponseModel.builder()
                .status(HttpStatus.OK)
                .message("Most Popular TimeSlots")
                .data(results.stream()
                        .map(result -> (TimeSlot) result[0])  // result[0] is the TimeSlot
                        .limit(limit)                         // limit the number of results
                        .collect(Collectors.toList()))
                .build();
    }
}
