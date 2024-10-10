package com.sporton.SportOn.service.bookingService;

import com.sporton.SportOn.dto.MonthlyIncome;
import com.sporton.SportOn.dto.MonthlyIncomeDTO;
import com.sporton.SportOn.entity.*;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.bookingModel.*;
import com.sporton.SportOn.repository.*;
import com.sporton.SportOn.service.notificationService.NotificationService;
import com.sporton.SportOn.service.venueService.VenueRevenueResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{

    private final TimeSlotRepository timeSlotRepository;
    private final BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;
    private  final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private NotificationService notificationService;

//

    @Override
    public CommonResponseModel bookCourt(BookingRequest body, String phoneNumber) throws CommonException {
        try {
            // Validate the user
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) throw new CommonException("User Is Not Found");

            AppUser user = optionalAppUser.get();

            // Validate the court
            Optional<Court> optionalCourt = courtRepository.findById(body.getCourtId());
            if (optionalCourt.isEmpty()) throw new CommonException("Court With Id " + body.getCourtId() + " Does Not Exist");

            // Validate the venue
            Optional<Venue> optionalVenue = venueRepository.findById(optionalCourt.get().getVenueId());
            if (optionalVenue.isEmpty()) throw new CommonException("Invalid Venue");

            Venue venue = optionalVenue.get();

            // Check the type of booking and handle accordingly
            if (body.getBookingType() == BookingType.ONE_TIME) {
                // Handle one-time booking
                return handleOneTimeBooking(body, user, venue);
            } else if (body.getBookingType() == BookingType.WEEKLY || body.getBookingType() == BookingType.MONTHLY) {
                // Handle recurring booking
                handleMultipleRecurringBooking(body, user, venue);
                return CommonResponseModel.builder()
                        .status(HttpStatus.CREATED)
                        .message("Recurring Booking Placed Successfully")
                        .build();
            } else {
                throw new CommonException("Unsupported booking type");
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    // handle recurring booking
    public void handleMultipleRecurringBooking(BookingRequest body, AppUser user, Venue venue) throws CommonException {
        LocalDate startDate = body.getMatchDate();
        LocalDate endDate = body.getRecurrenceEndDate();
        List<DayOfWeek> recurrenceDays = body.getRecurrenceDays();
        log.info("Inside recurring booking");
        log.info("Booking type: {}", body.getBookingType());

        // Generate the first booking as the parent booking for recurrence
        Booking parentBooking = null;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (recurrenceDays.contains(currentDate.getDayOfWeek())) {
                log.info("Booking for date: {}", currentDate);

                // If it's the first booking, save it as the parent booking
                if (parentBooking == null) {
                    parentBooking = saveBooking(body, user, venue, currentDate, null); // null because it's the first instance
                } else {
                    saveBooking(body, user, venue, currentDate, parentBooking); // Link subsequent bookings to the parent
                }
            }
            // Move to the next day
            currentDate = currentDate.plusDays(1);
        }
    }


    // Saving booking with availability check
    private Booking saveBooking(BookingRequest body, AppUser user, Venue venue, LocalDate matchDate, Booking parentBooking) throws CommonException {
        Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(body.getTimeSlotId());
        if (optionalTimeSlot.isEmpty()) {
            throw new CommonException("Time Slot with id " + body.getTimeSlotId() + " does not exist");
        }

        TimeSlot timeSlot = optionalTimeSlot.get();
        List<LocalDate> bookedDates = timeSlot.getBookedDates();

        if (!bookedDates.contains(matchDate)) {
            // Add the match date to the booked dates
            bookedDates.add(matchDate);
            timeSlot.setBookedDates(bookedDates);
            timeSlotRepository.save(timeSlot);

            // Create and save the booking
            Booking booking = Booking.builder()
                    .courtId(body.getCourtId())
                    .timeSlotId(body.getTimeSlotId())
                    .matchDate(matchDate)
                    .bookingDate(LocalDate.now())
                    .totalPrice(body.getTotalPrice())
                    .userId(user.getId())
                    .providerId(venue.getProviderId())
                    .paymentDueDate(LocalDate.now().plusMonths(1))
                    .status(BookingStatus.Pending)
                    .venue(venue)
                    .parentBooking(parentBooking)  // Link to the parent booking for recurrence
                    .build();
            bookingRepository.save(booking);
            return booking;  // Return the saved booking
        } else {
            throw new CommonException("This time slot "+ timeSlot.getStartTime()+" - "+timeSlot.getEndTime()+" is already booked on " + matchDate+" choose another date");
        }
    }

// handle one time booking
    private CommonResponseModel handleOneTimeBooking(BookingRequest body, AppUser user, Venue venue) throws CommonException {
        // Validate the time slot
        Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(body.getTimeSlotId());
        if (optionalTimeSlot.isEmpty()) {
            throw new CommonException("Time Slot With id " + body.getTimeSlotId() + " does not exist");
        }

        TimeSlot timeSlot = optionalTimeSlot.get();
        List<LocalDate> bookedDates = timeSlot.getBookedDates();

        if (!bookedDates.contains(body.getMatchDate())) {
            // Save the booking date to the time slot
            bookedDates.add(body.getMatchDate());
            timeSlot.setBookedDates(bookedDates);
            timeSlotRepository.save(timeSlot);

            // Create the booking entry
            Booking booking = Booking.builder()
                    .courtId(body.getCourtId())
                    .timeSlotId(body.getTimeSlotId())
                    .matchDate(body.getMatchDate())
                    .bookingDate(LocalDate.now())
                    .totalPrice(body.getTotalPrice())
                    .userId(user.getId())
                    .providerId(venue.getProviderId())
                    .status(BookingStatus.Pending)
                    .bookingType(BookingType.ONE_TIME)
                    .venue(venue)
                    .build();
            bookingRepository.save(booking);

            return CommonResponseModel.builder()
                    .status(HttpStatus.CREATED)
                    .message("Booking Placed Successfully")
                    .build();
        } else {
            throw new CommonException("This Time Slot, The Court Is Not Available");
        }
    }

//



    @Override
    public List<Object> getBookingByCustomerId(String phoneNumber, Integer page, Integer size) throws CommonException {
        // Fetch the user by phone number
        Optional<AppUser> userOptional = appUserRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new CommonException("User not found");
        }

        // Prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch bookings by user ID
        Optional<List<Booking>> allBookingsPage = bookingRepository.findByUserId(pageable, userOptional.get().getId());
        List<Booking> allBookings = allBookingsPage.get();

        // Separate the bookings into one-time and recurring
        List<Booking> oneTimeBookings = new ArrayList<>();
        Map<Long, List<Booking>> groupedRecurringBookings = new HashMap<>();

        for (Booking booking : allBookings) {
            if (booking.getParentBooking() == null) {
                // This is a one-time booking because it has no parent booking
                oneTimeBookings.add(booking);
            } else {
                // This is a recurring booking because it has a parent booking
                Long parentBookingId = booking.getParentBooking().getId();
                groupedRecurringBookings
                        .computeIfAbsent(parentBookingId, k -> new ArrayList<>())
                        .add(booking);
            }
        }

        // Prepare the response list
        List<Object> responseList = new ArrayList<>();
        responseList.add("oneTimeBookings");
        responseList.add(oneTimeBookings);

        // Group recurring bookings by parent booking ID
        List<Map<String, Object>> recurringBookingsList = new ArrayList<>();
        for (Map.Entry<Long, List<Booking>> entry : groupedRecurringBookings.entrySet()) {
            Map<String, Object> recurringGroup = new HashMap<>();
            recurringGroup.put("parentBookingId", entry.getKey());
            recurringGroup.put("recurringBookings", entry.getValue());
            recurringBookingsList.add(recurringGroup);
        }

        responseList.add("recurringBookings");
        responseList.add(recurringBookingsList);

        return responseList;
    }

    @Override
    public CommonResponseModel updateBooking(Long bookingId, BookingRequest body) throws CommonException {
        try {
            Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
            if (optionalBooking.isPresent()){
                optionalBooking.get()
                        .setMatchDate(body.getMatchDate());
                Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(body.getTimeSlotId());
                if (optionalTimeSlot.isPresent()){
                    if (optionalTimeSlot.get().getAvailable()){
                        optionalBooking.get().setTimeSlotId(body.getTimeSlotId());
                    }else {
                        throw new CommonException("This Time Slot, The Court Is Not Available");
                    }
                }else {
                    throw new CommonException("Time Slot With id " + body.getTimeSlotId() + " does not exit");
                }
                bookingRepository.save(optionalBooking.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Booking Updated Successfully")
                        .build();
            }else {
                throw new CommonException("There Is No Booking With Id " + bookingId);
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel cancelBooking(Long bookingId) throws CommonException {
        try {
            Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
            if (optionalBooking.isPresent()){
                Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(optionalBooking.get().getTimeSlotId());
                optionalTimeSlot.get().getBookedDates().remove(optionalBooking.get().getMatchDate());
//                cancel logic happens here
                optionalBooking.get().setStatus(BookingStatus.Canceled);
                bookingRepository.save(optionalBooking.get());
                timeSlotRepository.save(optionalTimeSlot.get());
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("Booking Canceled Successfully")
                        .build();
            }else {
                throw new CommonException("There Is No Booking With Id " + bookingId);
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel confirmtBooking(Long bookingId) throws CommonException {
        try {
            Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
            if (optionalBooking.isPresent()){
//                cancel logic happens here
                if (optionalBooking.get().getStatus() == BookingStatus.Pending) {
                    // Get current date and time
                    LocalDate currentDate = LocalDate.now();
                    LocalTime currentTime = LocalTime.now();

                    // Retrieve the matchDate and timeSlot from the booking
                    LocalDate matchDate = optionalBooking.get().getMatchDate();
                    TimeSlot timeSlot = timeSlotRepository.findById(optionalBooking.get().getTimeSlotId())
                            .orElseThrow(() -> new CommonException("Time slot not found"));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    // Parse the timeSlot's startTime and endTime from String to LocalTime
                    LocalTime slotStartTime = LocalTime.parse(timeSlot.getStartTime(), formatter);
                    LocalTime slotEndTime = LocalTime.parse(timeSlot.getEndTime(), formatter);

                    // Check if matchDate is before the current date
                    if (matchDate.isBefore(currentDate) ||
                            (matchDate.isEqual(currentDate) && slotEndTime.isBefore(currentTime))) {
                        optionalBooking.get().setStatus(BookingStatus.Expired);
                        bookingRepository.save(optionalBooking.get());
                        throw new CommonException("The booking has expired and cannot be confirmed");
                    }

                    // Update the booking status to Confirmed
                    optionalBooking.get().setStatus(BookingStatus.Confirmed);
                    bookingRepository.save(optionalBooking.get());

                    return CommonResponseModel.builder()
                            .status(HttpStatus.OK)
                            .message("Booking Confirmed Successfully")
                            .build();
                } else {
                    throw new CommonException("The order is not a Pending order: " + bookingId);
                }

            }else {
                throw new CommonException("There Is No Booking With Id " + bookingId);
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<ProviderOrderResponseDTO> getBookingsByProviderId(String phoneNumber , Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            List<Booking> bookings = optionalBookings.get().stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.Pending)
                    .collect(Collectors.toList());

            if (bookings.isEmpty()) {
                throw new CommonException("No pending bookings found for provider");
            }

            List<ProviderOrderResponseDTO> providerOrderResponseDTOs = bookings.stream().map(booking -> {
                Venue venue = booking.getVenue();
                Court court = null;
                try {
                    court = courtRepository.findById(booking.getCourtId())
                            .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                } catch (CommonException e) {
                    throw new RuntimeException(e);
                }

                TimeSlot timeSlot = null;
                try {
                    timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                            .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                } catch (CommonException e) {
                    throw new RuntimeException(e);
                }

                String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                return ProviderOrderResponseDTO.builder()
                        .venueId(venue.getId())
                        .orderId(booking.getId())
                        .venueName(venue.getName())
                        .bookingDate(booking.getBookingDate())
                        .matchDate(booking.getMatchDate())
                        .courtName(court.getName())
                        .userPhoneNumber(optionalAppUser.get().getPhoneNumber())
                        .userName(optionalAppUser.get().getFullName())
                        .startTime(timeSlot.getStartTime())
                        .endTime(timeSlot.getEndTime())
                        .totalPrice(booking.getTotalPrice())
                        .status(BookingStatus.valueOf(booking.getStatus().name()))
                        .userProfileImage(optionalAppUser.get().getProfileImage())
                        .userProfileImage(firstImage)
                        .build();
            }).collect(Collectors.toList());

            return providerOrderResponseDTOs;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getBookingsByUserId(String phoneNumber, Integer page, Integer size) throws CommonException {
        // Fetch the user by phone number
        Optional<AppUser> userOptional = appUserRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new CommonException("User not found");
        }

        // Prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch bookings by user ID
        Optional<List<Booking>> allBookingsPage = bookingRepository.findByUserId(pageable, userOptional.get().getId());
        List<Booking> allBookings = allBookingsPage.get();

        // Separate the bookings into one-time and recurring
        List<Booking> oneTimeBookings = new ArrayList<>();
        Map<Long, List<Booking>> groupedRecurringBookings = new HashMap<>();

        for (Booking booking : allBookings) {
            if (booking.getBookingType() == BookingType.ONE_TIME) {
                oneTimeBookings.add(booking);
            } else {
                // Group recurring bookings by the parent booking ID
                Long parentBookingId = booking.getParentBooking() != null
                        ? booking.getParentBooking().getId()
                        : booking.getId();
                groupedRecurringBookings
                        .computeIfAbsent(parentBookingId, k -> new ArrayList<>())
                        .add(booking);
            }
        }

        // Prepare the response map
        Map<String, Object> response = new HashMap<>();
        response.put("oneTimeBookings", oneTimeBookings);

        // Group recurring bookings by parent booking ID
        List<Map<String, Object>> recurringBookingsList = new ArrayList<>();
        for (Map.Entry<Long, List<Booking>> entry : groupedRecurringBookings.entrySet()) {
            Map<String, Object> recurringGroup = new HashMap<>();
            recurringGroup.put("parentBookingId", entry.getKey());
            recurringGroup.put("recurringBookings", entry.getValue());
            recurringBookingsList.add(recurringGroup);
        }

        response.put("recurringBookings", recurringBookingsList);

        return response;
    }
    public CommonResponseModel getNumberOfNewOrders(String phoneNumber, Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            // Get today's date
            LocalDate today = LocalDate.now();

            // Filter bookings that are created today and are pending
            List<Booking> bookings = optionalBookings.get().stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.Pending)
                    .filter(booking -> booking.getBookingDate().isEqual(today))
                    .collect(Collectors.toList());

            int pendingOrdersCount = bookings.size();
            if (pendingOrdersCount == 0) {
                throw new CommonException("No pending bookings found for provider");
            }

            List<ProviderOrderResponseDTO> topPendingOrders = bookings.stream()
                    .sorted(Comparator.comparing(Booking::getBookingDate).reversed())
                    .limit(10)
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        AppUser user = bookingUser.get();
                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(BookingStatus.valueOf(booking.getStatus().name()))
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            ProviderOrdersResponse response = new ProviderOrdersResponse(pendingOrdersCount, topPendingOrders);
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(response)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getPendingOrders(String phoneNumber, Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

            Optional<List<Booking>> allBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            List<Booking> pendingBookings = new ArrayList<>();

            for (Booking booking : allBookings.get()) {
                if (booking.getStatus() == BookingStatus.Pending) {
                    pendingBookings.add(booking);
                }
            }

            if (pendingBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No pending orders found")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> pendingOrders = pendingBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        AppUser user = bookingUser.get();

                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(pendingOrders)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getExpiredOrders(String phoneNumber, Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

            Optional<List<Booking>> allBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            List<Booking> pendingBookings = new ArrayList<>();

            for (Booking booking : allBookings.get()) {
                if (booking.getStatus() == BookingStatus.Expired) {
                    pendingBookings.add(booking);
                }
            }

            if (pendingBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No expired orders found")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> pendingOrders = pendingBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        AppUser user = bookingUser.get();

                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(pendingOrders)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getCompletedOrders(String phoneNumber, Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());

            Optional<List<Booking>> allBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            List<Booking> pendingBookings = new ArrayList<>();

            for (Booking booking : allBookings.get()) {
                if (booking.getStatus() == BookingStatus.Completed) {
                    pendingBookings.add(booking);
                }
            }

            if (pendingBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No completed orders found")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> pendingOrders = pendingBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        AppUser user = bookingUser.get();

                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(pendingOrders)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getConfirmedOrders(String phoneNumber, Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> allBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            List<Booking> confirmedBookings = new ArrayList<>();

            for (Booking booking : allBookings.get()) {
                if (booking.getStatus() == BookingStatus.Confirmed) {
                    confirmedBookings.add(booking);
                }
            }

            if (confirmedBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No pending orders found")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> pendingOrders = confirmedBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        AppUser user = bookingUser.get();
                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(pendingOrders)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getCancelledOrders(String phoneNumber , Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> allBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            List<Booking> confirmedBookings = new ArrayList<>();

            for (Booking booking : allBookings.get()) {
                if (booking.getStatus() == BookingStatus.Canceled) {
                    confirmedBookings.add(booking);
                }
            }

            if (confirmedBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No pending orders found")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> pendingOrders = confirmedBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }

                        String firstImage = venue.getImages() != null && !venue.getImages().isEmpty() ? venue.getImages().get(0) : null;

                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        AppUser user = bookingUser.get();

                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(optionalAppUser.get().getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Pending orders retrieved successfully")
                    .data(pendingOrders)
                    .build();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getNumberOfTodayOrders(String phoneNumber , Integer page, Integer size) {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            // Get today's date
            LocalDate today = LocalDate.now();

            // Filter bookings that are created today
            long todayOrdersCount = optionalBookings.get().stream()
                    .filter(booking -> booking.getBookingDate().isEqual(today))
                    .count();

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Number of orders retrieved successfully")
                    .data(todayOrdersCount)
                    .build();
        } catch (Exception e) {
            return CommonResponseModel.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CommonResponseModel getNumberOfTodayMatches(String phoneNumber , Integer page, Integer size) {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            // Get today's date
            LocalDate today = LocalDate.now();

            // Filter bookings that have a match scheduled for today and matchDate is not null
            long todayMatchesCount = optionalBookings.get().stream()
                    .filter(booking -> (booking.getMatchDate() != null && booking.getMatchDate().isEqual(today) )&& booking.getStatus()==BookingStatus.Confirmed)
                    .count();

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Number of matches scheduled for today retrieved successfully")
                    .data(todayMatchesCount)
                    .build();
        } catch (Exception e) {
            return CommonResponseModel.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CommonResponseModel getNumberOfPendingOrders(String phoneNumber, Integer page, Integer size) {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());
            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            // Get today's date
            LocalDate today = LocalDate.now();

            // Filter bookings that have status of pending
            long pendingOrdersCount = optionalBookings.get().stream()
                    .filter(booking -> booking.getStatus()==BookingStatus.Pending)
                    .count();

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Number of pending orders retrieved successfully")
                    .data(pendingOrdersCount)
                    .build();
        } catch (Exception e) {
            return CommonResponseModel.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CommonResponseModel getMatchesByDate(String phoneNumber, MatchesRequestModel body , Integer page, Integer size) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }

            AppUser appUser = optionalAppUser.get();
            LocalDate targetDate = convertToLocalDate(body.getDate());
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            List<Booking> confirmedBookings = bookingRepository.findByProviderId(pageable, appUser.getId())
                    .orElseThrow(() -> new CommonException("No bookings found for user"))
                    .stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.Confirmed)
                    .filter(booking -> booking.getMatchDate().equals(targetDate))
                    .collect(Collectors.toList());

            if (confirmedBookings.isEmpty()) {
                return CommonResponseModel.builder()
                        .status(HttpStatus.OK)
                        .message("No matches found for the specified date")
                        .data(Collections.emptyList())
                        .build();
            }

            List<ProviderOrderResponseDTO> matches = confirmedBookings.stream()
                    .map(booking -> {
                        Venue venue = booking.getVenue();
                        Court court = null;
                        try {
                            court = courtRepository.findById(booking.getCourtId())
                                    .orElseThrow(() -> new CommonException("Court with id " + booking.getCourtId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }
                        TimeSlot timeSlot = null;
                        try {
                            timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                                    .orElseThrow(() -> new CommonException("Time slot with id " + booking.getTimeSlotId() + " does not exist"));
                        } catch (CommonException e) {
                            throw new RuntimeException(e);
                        }
                        Optional<AppUser> bookingUser = appUserRepository.findById(booking.getUserId());
                        if (bookingUser.isEmpty()) {
                            try {
                                throw new CommonException("User with id " + booking.getUserId() + " does not exist");
                            } catch (CommonException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        AppUser user = bookingUser.get();

                        String firstImage = (venue.getImages() != null && !venue.getImages().isEmpty()) ? venue.getImages().get(0) : null;

                        return ProviderOrderResponseDTO.builder()
                                .venueId(venue.getId())
                                .orderId(booking.getId())
                                .venueName(venue.getName())
                                .bookingDate(booking.getBookingDate())
                                .matchDate(booking.getMatchDate())
                                .courtName(court.getName())
                                .userPhoneNumber(user.getPhoneNumber())
                                .userName(user.getFullName())
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .totalPrice(booking.getTotalPrice())
                                .status(booking.getStatus())
                                .userProfileImage(user.getProfileImage())
                                .build();
                    })
                    .collect(Collectors.toList());

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Matches retrieved successfully")
                    .data(matches)
                    .build();

        } catch (Exception e) {
            throw new CommonException("Error retrieving matches: " + e.getMessage());
        }
    }


    public LocalDate convertToLocalDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }

    public CommonResponseModel getTotalIncomeByPeriod(String periodType) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (periodType.toLowerCase()) {
            case "daily":
                startDate = endDate;
                break;
            case "weekly":
                startDate = endDate.minusDays(6);
                break;
            case "monthly":
                startDate = endDate.minusMonths(1).plusDays(1);
                break;
            case "yearly":
                startDate = endDate.minusYears(1).plusDays(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid period type. Use 'DAILY', 'WEEKLY', 'MONTHLY', or 'YEARLY'.");
        }
        return CommonResponseModel.builder()
                .status(HttpStatus.OK)
                .message("Total Booking Income Retrieved Successfully")
                .data(bookingRepository.findTotalIncomeByDateRange(BookingStatus.Confirmed, startDate, endDate))
                .build();
    }

    @Override
    public CommonResponseModel getLast12MonthsIncome(String phoneNumber) throws CommonException {
        try {
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }
            LocalDate startDate = LocalDate.now().minusMonths(12);

            YearMonth currentMonth = YearMonth.now();
            List<YearMonth> last12Months = IntStream.range(0, 12)
                    .mapToObj(currentMonth::minusMonths)
                    .collect(Collectors.toList());

            // Fetch income data from the repository
            List<MonthlyIncome> incomeData = bookingRepository.findMonthlyIncome(currentMonth.minusMonths(12).atDay(1));

            // Map fetched data into a map for easy lookup
            Map<YearMonth, BigDecimal> incomeMap = incomeData.stream()
                    .collect(Collectors.toMap(
                            mi -> YearMonth.of(mi.getYear(), mi.getMonth()),
                            MonthlyIncome::getTotalIncome
                    ));

            // Merge, fill missing months, and format month name
            List<MonthlyIncomeDTO> allMonthsIncome =  last12Months.stream()
                    .map(month -> {
                        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();
                        BigDecimal income = incomeMap.getOrDefault(month, BigDecimal.ZERO);
                        return new MonthlyIncomeDTO(monthName, income);
                    })
                    .sorted((a, b) -> a.getMonthName().compareTo(b.getMonthName())) // Optional: already in order
                    .collect(Collectors.toList());


            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Booking Income By Monthly Retrieved Successfully")
                    .data(allMonthsIncome)
                    .build();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalIncomeForProvider(String phoneNumber) throws CommonException {
        try {
            // Fetch provider by phone number
            Optional<AppUser> provider = appUserRepository.findByPhoneNumber(phoneNumber);
            if (provider == null) {
                throw new  CommonException("Provider not found", HttpStatus.NOT_FOUND);
            }

            // Get total income from completed bookings for this provider
            Double totalIncome = bookingRepository.findTotalIncomeForProvider(provider.get().getId(), BookingStatus.Confirmed);

            // If no bookings found, return zero income
            if (totalIncome == null) {
                totalIncome = 0.0;
            }
return CommonResponseModel.builder()
        .status(HttpStatus.OK)
        .message("Total Provider Income Retrieved Successfully")
        .data(totalIncome)
        .build();
            // Return the total income in a response model
//            return new CommonException("Total income calculated", totalIncome 200, );
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }

    }

    public CommonResponseModel getTotalVenueIncome(Long venueId, String period) {
        Double totalIncome;

        // If no period is provided, fetch the total income for all completed bookings
        if (period == null || period.isEmpty()) {
            totalIncome = bookingRepository.findTotalIncomeForVenue(venueId);
        } else {
            // Otherwise, calculate income based on the specified period (monthly or yearly)
            LocalDate startDate;
            LocalDate endDate = LocalDate.now(); // End date is today

            if (period.equalsIgnoreCase("monthly")) {
                startDate = endDate.withDayOfMonth(1); // First day of the current month
            } else if (period.equalsIgnoreCase("yearly")) {
                startDate = endDate.withDayOfYear(1); // First day of the current year
            } else {
                throw new IllegalArgumentException("Invalid period. Use 'monthly', 'yearly', or leave empty.");
            }

            // Fetch the total income for the venue based on the time period
            totalIncome = bookingRepository.findTotalIncomeForVenueInPeriod(venueId, startDate, endDate);
        }

        // If there are no completed bookings, return income as zero
        if (totalIncome == null) {
            totalIncome = 0.0;
        }

        // Return the total income in a response model
        return CommonResponseModel.builder()
                .status(HttpStatus.OK)
                .message("Total income for venue calculated successfully")
                .data(totalIncome)
                .build();
    }

    @Override
    public CommonResponseModel getTotalIncomeOfTodayOrders(String phoneNumber, int page, int size) {
        try {
            // Retrieve the AppUser by phone number
            Optional<AppUser> optionalAppUser = appUserRepository.findByPhoneNumber(phoneNumber);
            if (optionalAppUser.isEmpty()) {
                throw new CommonException("User with phone number " + phoneNumber + " does not exist");
            }

            // Set pagination and sorting by bookingDate
            Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
            Optional<List<Booking>> optionalBookings = bookingRepository.findByProviderId(pageable, optionalAppUser.get().getId());

            if (optionalBookings.isEmpty() || optionalBookings.get().isEmpty()) {
                throw new CommonException("No bookings found for provider");
            }

            // Get today's date
            LocalDate today = LocalDate.now();

            // Filter bookings for today and calculate the total income of "Confirmed" or "Completed" bookings
            double totalIncome = optionalBookings.get().stream()
                    .filter(booking -> booking.getBookingDate().isEqual(today))   // Filter by today's bookings
                    .filter(booking -> {
                        String status = String.valueOf(booking.getStatus());
                        return "Confirmed".equals(status) || "Completed".equals(status);  // Filter by Confirmed or Completed status
                    })
                    .mapToDouble(Booking::getTotalPrice)  // Assuming getTotalPrice() returns the price of each booking
                    .sum();

            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total income of today's confirmed or completed orders retrieved successfully")
                    .data(totalIncome)
                    .build();
        } catch (Exception e) {
            return CommonResponseModel.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public CommonResponseModel getTotalBookings(Long venueId, LocalDate startDate, LocalDate endDate) throws CommonException {
        try {
            List<Booking> bookings = bookingRepository.findBookingsByVenueAndPeriod(venueId, startDate, endDate);
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total Booking For Venue")
                    .data(bookings.size())
                    .build();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public CommonResponseModel getTotalRevenue(Long venueId, LocalDate startDate, LocalDate endDate) throws VenueException {
        try {
            Double totalRevenue = bookingRepository.calculateTotalRevenueByVenueAndDateAndStatus(venueId, startDate, endDate, List.of(BookingStatus.Confirmed, BookingStatus.Completed));
            return CommonResponseModel.builder()
                    .status(HttpStatus.OK)
                    .message("Total Revenue For a Venue")
                    .data(totalRevenue != null ? totalRevenue : 0.0)
                    .build();
        }catch (Exception e){
            throw new VenueException(e.getMessage());
        }
    }

    @Override
    public void createOneTimeBooking(Booking booking) {
        booking.setBookingDate(booking.getMatchDate());
        booking.setBookingType(BookingType.ONE_TIME);
        bookingRepository.save(booking);
    }

    @Override
    public VenueRevenueResult getVenueWithHighestRevenue(String startDate, String endDate) throws CommonException {
        try {
            // Define the date formatter
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Parse the start and end dates
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Fetch confirmed bookings filtered by date
            List<Booking> confirmedBookings = bookingRepository.findByStatusInAndBookingDateBetween(
                    List.of("Confirmed", "Completed"), start, end);

            // Group by venue and sum their revenues
            Map<Long, Double> revenueMap = confirmedBookings.stream()
                    .collect(Collectors.groupingBy(
                            Booking::getCourtId,  // Assuming getCourtId() returns the venue ID
                            Collectors.summingDouble(Booking::getTotalPrice)));

            // Find the entry with the maximum revenue
            Map.Entry<Long, Double> highestRevenueEntry = revenueMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow(() -> new RuntimeException("No confirmed or completed bookings found"));

            Long highestRevenueVenueId = highestRevenueEntry.getKey();
            Double highestRevenueAmount = highestRevenueEntry.getValue();

            // Fetch the venue details
            Venue venue = venueRepository.findById(highestRevenueVenueId)
                    .orElseThrow(() -> new RuntimeException("Venue not found"));

            // Return the result as a custom DTO
            return new VenueRevenueResult(venue, highestRevenueAmount);

        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public List<BookedVenueResponseDTO> getOneTimeBookings(String phoneNumber, int page, int size) throws CommonException {
        // Fetch the user by phone number
        Optional<AppUser> userOptional = appUserRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new CommonException("User not found");
        }

        // Prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch bookings by user ID and filter out one-time bookings
        Optional<List<Booking>> allBookingsPage = bookingRepository.findByUserId(pageable, userOptional.get().getId());
        List<Booking> allBookings = allBookingsPage.orElse(new ArrayList<>());

        // Collect one-time bookings based on the bookingType field
        List<Booking> oneTimeBookings = allBookings.stream()
                .filter(booking -> booking.getBookingType() == BookingType.ONE_TIME)
                .collect(Collectors.toList());

        // Map to DTO
        List<BookedVenueResponseDTO> bookingDTOs = new ArrayList<>();
        for (Booking booking : oneTimeBookings) {
            // Fetch court details using courtId
            Court court = courtRepository.findById(booking.getCourtId())
                    .orElseThrow(() -> new CommonException("Court not found"));

            // Fetch time slot details using timeSlotId
            TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                    .orElseThrow(() -> new CommonException("Time slot not found"));

            // Map booking to DTO
            BookedVenueResponseDTO dto = BookedVenueResponseDTO.builder()
                    .id(booking.getId())
                    .venueId(booking.getVenue().getId())
                    .venueName(booking.getVenue().getName())
                    .courtName(court.getName())  // Use fetched court name
                    .bookingDate(booking.getBookingDate())
                    .matchDate(booking.getMatchDate())
                    .totalPrice(booking.getTotalPrice())
                    .status(booking.getStatus())
                    .venuePhoneNumber(booking.getVenue().getPhoneNumber())
                    .startTime(timeSlot.getStartTime())  // Use fetched time slot start time
                    .endTime(timeSlot.getEndTime())      // Use fetched time slot end time
                    .image(booking.getVenue().getImages().isEmpty() ? null : booking.getVenue().getImages().get(0))  // Assuming first image
                    .build();

            bookingDTOs.add(dto);
        }

        return bookingDTOs;
    }

    @Override
    public List<Map<String, Object>> getRecurringBookings(String phoneNumber, int page, int size) throws CommonException {
        // Fetch the user by phone number
        Optional<AppUser> userOptional = appUserRepository.findByPhoneNumber(phoneNumber);
        if (!userOptional.isPresent()) {
            throw new CommonException("User not found");
        }

        // Prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch bookings by user ID and group recurring bookings
        Optional<List<Booking>> allBookingsPage = bookingRepository.findByUserId(pageable, userOptional.get().getId());
        List<Booking> allBookings = allBookingsPage.orElse(new ArrayList<>());

        // Group recurring bookings by parent booking ID (excluding ONE_TIME bookings)
        Map<Long, List<Booking>> groupedRecurringBookings = new HashMap<>();
        for (Booking booking : allBookings) {
            if (booking.getBookingType() != BookingType.ONE_TIME) {
                Long parentBookingId = booking.getParentBooking() != null
                        ? booking.getParentBooking().getId()
                        : booking.getId();
                groupedRecurringBookings
                        .computeIfAbsent(parentBookingId, k -> new ArrayList<>())
                        .add(booking);
            }
        }

        // Prepare the response
        List<Map<String, Object>> recurringBookingsList = new ArrayList<>();
        for (Map.Entry<Long, List<Booking>> entry : groupedRecurringBookings.entrySet()) {
            List<BookedVenueResponseDTO> bookingDTOList = new ArrayList<>();

            for (Booking booking : entry.getValue()) {
                // Fetch court details using courtId
                Court court = courtRepository.findById(booking.getCourtId())
                        .orElseThrow(() -> new CommonException("Court not found"));

                // Fetch time slot details using timeSlotId
                TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                        .orElseThrow(() -> new CommonException("Time slot not found"));

                // Map the booking to DTO
                BookedVenueResponseDTO dto = BookedVenueResponseDTO.builder()
                        .id(booking.getId())
                        .venueId(booking.getVenue().getId())
                        .venueName(booking.getVenue().getName())
                        .courtName(court.getName())  // Use fetched court name
                        .bookingDate(booking.getBookingDate())
                        .matchDate(booking.getMatchDate())
                        .totalPrice(booking.getTotalPrice())
                        .status(booking.getStatus())
                        .venuePhoneNumber(booking.getVenue().getPhoneNumber())
                        .startTime(timeSlot.getStartTime())  // Use fetched time slot start time
                        .endTime(timeSlot.getEndTime())      // Use fetched time slot end time
                        .image(booking.getVenue().getImages().get(0))  // Assuming first image
                        .build();

                bookingDTOList.add(dto);
            }

            // Create the recurring booking group
            Map<String, Object> recurringGroup = new HashMap<>();
            recurringGroup.put("parentBookingId", entry.getKey());
            recurringGroup.put("recurringBookings", bookingDTOList);
            recurringBookingsList.add(recurringGroup);
        }

        return recurringBookingsList;
    }


    @Scheduled(fixedRate = 3600000) // Every hour (in milliseconds)
    public void updateExpiredBookings() throws CommonException {
        try {
            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();

            // Find all confirmed bookings
            List<Booking> confirmedBookings = bookingRepository.findByStatus(BookingStatus.Confirmed);

            for (Booking booking : confirmedBookings) {
                // Check if matchDate and endTime of the timeSlot have passed
                if (booking.getMatchDate().isBefore(currentDate) ||
                        (booking.getMatchDate().isEqual(currentDate) && hasMatchEnded(booking.getTimeSlotId(), currentTime))) {
                    booking.setStatus(BookingStatus.Completed);
                    bookingRepository.save(booking);  // Update the status to Completed
                }
            }
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    // Helper method to check if the time slot's end time has passed
    private boolean hasMatchEnded(Long timeSlotId, LocalTime currentTime) throws CommonException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a"); // 'a' for AM/PM
//            LocalTime time = LocalTime.parse(timeString, formatter);
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new RuntimeException("Time slot not found"));
            LocalTime slotEndTime = LocalTime.parse(timeSlot.getEndTime(), formatter); // Parse endTime from String to LocalTime
            return slotEndTime.isBefore(currentTime);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }


    @Scheduled(cron = "0 0 9 * * ?") // Daily check
    public void checkForOverduePayments() {
        LocalDate currentDate = LocalDate.now();
        List<Booking> overdueBookings = bookingRepository.findOverdueBookings(currentDate, BookingStatus.Pending);

        for (Booking booking : overdueBookings) {
            booking.setStatus(BookingStatus.Canceled);
            bookingRepository.save(booking);
            Long userId = booking.getUserId();
            Optional<AppUser> user = appUserRepository.findAppUserById(userId);
            // Notify the user of cancellation
            notificationService.sendBookingCancellation(user.get(), booking);
        }
    }
}