package com.sporton.SportOn.controller;

import com.sporton.SportOn.configuration.JWTService;
import com.sporton.SportOn.entity.Booking;
import com.sporton.SportOn.entity.TimeSlot;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.bookingModel.BookedVenueResponseDTO;
import com.sporton.SportOn.model.bookingModel.BookingRequest;
import com.sporton.SportOn.model.bookingModel.MatchesRequestModel;
import com.sporton.SportOn.model.bookingModel.ProviderOrderResponseDTO;
import com.sporton.SportOn.repository.TimeSlotRepository;
import com.sporton.SportOn.service.bookingService.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final JWTService jwtService;
    private final TimeSlotRepository timeSlotRepository;
    @PostMapping("/book")
    public CommonResponseModel bookCourt(
            @RequestBody BookingRequest body,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.bookCourt(body, phoneNumber);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/get")
    public List<BookedVenueResponseDTO> getBookingsByUserId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getBookingsByUserId(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getBookingByProviderId")
    public List<ProviderOrderResponseDTO> getBookingsByProviderId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getBookingsByProviderId(phoneNumber , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getBookingByCustomerId")
    public List<BookedVenueResponseDTO> getBookingByCustomerId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getBookingsByUserId(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/update/{bookingId}")
    public CommonResponseModel updateBooking(@PathVariable Long bookingId, @RequestBody BookingRequest body) throws CommonException {
        try {
            return bookingService.updateBooking(bookingId, body);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/cancel/{bookingId}")
    public CommonResponseModel cancelBooking(@PathVariable Long bookingId) throws CommonException {
        try {
            return bookingService.cancelBooking(bookingId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PutMapping("/accept/{bookingId}")
    public CommonResponseModel acceptBooking(@PathVariable Long bookingId) throws CommonException {
        try {
            return bookingService.confirmtBooking(bookingId);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getTop10NewOrders")
    public CommonResponseModel getNumberOfNewOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getNumberOfNewOrders(phoneNumber , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getPendingOrders")
    public CommonResponseModel getPendingOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getPendingOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getExpiredOrders")
    public CommonResponseModel getExpiredOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getExpiredOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getCompletedOrders")
    public CommonResponseModel getCompletedOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getCompletedOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getConfirmedOrders")
    public CommonResponseModel getConfirmedOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getConfirmedOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getCancelledOrders")
    public CommonResponseModel getCancelledOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getCancelledOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getNumberOfTodayOrders")
    public CommonResponseModel getNumberOfTodayOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getNumberOfTodayOrders(phoneNumber , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getNumberOfTodayMatches")
    public CommonResponseModel getNumberOfTodayMatches(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getNumberOfTodayMatches(phoneNumber , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getNumberOfPendingOrders")
    public CommonResponseModel getNumberOfPendingOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getNumberOfPendingOrders(phoneNumber, page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PostMapping("/getMatchesByDate")
    public CommonResponseModel getMatchesByDate(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestBody MatchesRequestModel body
            ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getMatchesByDate(phoneNumber, body , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/income")
    public CommonResponseModel getTotalIncomeByPeriod(@RequestParam String periodType) throws CommonException {
        try {
            return bookingService.getTotalIncomeByPeriod(periodType);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getLast12MonthsIncome")
    public CommonResponseModel getLast12MonthsIncome(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getLast12MonthsIncome(phoneNumber);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/totalBookingIncomeForProvider")
    public CommonResponseModel getTotalIncomeForProvider(
            @RequestHeader("Authorization") String authorizationHeader
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getTotalIncomeForProvider(phoneNumber);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getTotalVenueIncome/{venueId}")
    public CommonResponseModel getTotalVenueIncome(
            @PathVariable Long venueId,
            @RequestParam(value = "period", required = false) String period
    ) throws CommonException {
        try {
            return bookingService.getTotalVenueIncome(venueId, period);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @GetMapping("/getTotalIncomeOfTodayOrders")
    public CommonResponseModel getTotalIncomeOfTodayOrders(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) throws CommonException {
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String phoneNumber = jwtService.extractUsername(token);
            return bookingService.getTotalIncomeOfTodayOrders(phoneNumber , page, size);
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    @PostMapping("/one-time")
    public ResponseEntity<Booking> bookOneTime(@RequestBody Booking booking) {
        bookingService.createOneTimeBooking(booking);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/calculate")
    public ResponseEntity<Double> calculateBookingPrice(@RequestBody BookingRequest body) throws CommonException {
        // Fetch the price of the time slot
        Optional<TimeSlot> optionalTimeSlot = timeSlotRepository.findById(body.getTimeSlotId());
        if (optionalTimeSlot.isEmpty()) {
            throw new CommonException("Time Slot with id " + body.getTimeSlotId() + " does not exist");
        }

        TimeSlot timeSlot = optionalTimeSlot.get();
        double timeSlotPrice = timeSlot.getPrice(); // Assume there's a price field in TimeSlot

        double totalPrice = 0.0;

        // Calculate price based on booking type
        switch (body.getBookingType()) {
            case ONE_TIME:
                totalPrice = timeSlotPrice;
                break;

            case WEEKLY:
                // Calculate the number of weeks between the match date and recurrence end date
                long weeksBetween = ChronoUnit.WEEKS.between(body.getMatchDate(), body.getRecurrenceEndDate());
                totalPrice = timeSlotPrice * (weeksBetween + 1); // +1 to include the current week
                break;

            case MONTHLY:
                // Calculate the number of occurrences of the specified day in each month until the end date
                totalPrice = calculateMonthlyPrice(body, timeSlotPrice);
                break;

            default:
                throw new CommonException("Invalid booking type");
        }

        return ResponseEntity.ok(totalPrice);
    }

    // Helper method to calculate price for monthly bookings
    private double calculateMonthlyPrice(BookingRequest body, double timeSlotPrice) {
        double totalPrice = 0.0;

        LocalDate startDate = body.getMatchDate();
        LocalDate endDate = body.getRecurrenceEndDate();

        // Iterate through each month between the start date and end date
        while (!startDate.isAfter(endDate)) {
            // For each month, check how many occurrences of the selected day there are
            LocalDate firstDayOfMonth = startDate.withDayOfMonth(1);
            LocalDate lastDayOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

            while (!firstDayOfMonth.isAfter(lastDayOfMonth)) {
                if (firstDayOfMonth.getDayOfWeek().equals(body.getRecurrenceDay())) {
                    totalPrice += timeSlotPrice;
                }
                firstDayOfMonth = firstDayOfMonth.plusDays(1);
            }

            // Move to the next month
            startDate = startDate.plusMonths(1);
        }

        return totalPrice;
    }

}
