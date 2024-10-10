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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
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
            return (List<BookedVenueResponseDTO>) bookingService.getBookingsByUserId(phoneNumber, page, size);
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

//    @GetMapping("/getBookingByCustomerId")
//    public Map<String, Object> getBookingByCustomerId(
//            @RequestHeader("Authorization") String authorizationHeader,
//            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
//            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
//    ) throws CommonException {
//        try {
//            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
//            String phoneNumber = jwtService.extractUsername(token);
//            // Directly return the result without casting
//            return bookingService.getBookingsByUserId(phoneNumber, page, size);
//        } catch (Exception e) {
//            throw new CommonException(e.getMessage());
//        }
//    }

    @GetMapping("/getBookingByCustomerId")
    public ResponseEntity<List<Object>> getBookingByCustomerId(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) throws CommonException {

        // Extract phone number from JWT token
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String phoneNumber = jwtService.extractUsername(token);

        // Fetch the bookings
        List<Object> bookings = bookingService.getBookingByCustomerId(phoneNumber, page, size);

        return ResponseEntity.ok(bookings);
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size,
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
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
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
        double timeSlotPrice = timeSlot.getPrice();

        double totalPrice = 0.0;

        // Calculate price based on booking type
        switch (body.getBookingType()) {
            case ONE_TIME:
                totalPrice = timeSlotPrice;
                break;

            case WEEKLY:
                // Calculate the total price based on multiple selected days of the week
                totalPrice = calculateWeeklyPrice(body, timeSlotPrice);
                break;

            case MONTHLY:
                // Calculate the total price based on multiple selected days per month
                totalPrice = calculateMonthlyPrice(body, timeSlotPrice);
                break;

            default:
                throw new CommonException("Invalid booking type");
        }

        return ResponseEntity.ok(totalPrice);
    }

    // Helper method to calculate price for weekly bookings with multiple days
    // Calculate the total price for weekly bookings
    private double calculateWeeklyPrice(BookingRequest body, double timeSlotPrice) {
        double totalPrice = 0.0;
        LocalDate startDate = body.getMatchDate();
        LocalDate endDate = body.getRecurrenceEndDate();

        // Loop over each day between startDate and endDate
        while (!startDate.isAfter(endDate)) {
            for (DayOfWeek selectedDay : body.getRecurrenceDays()) {
                // Get the next occurrence of the selected day in the current week
                LocalDate nextOccurrence = startDate.with(TemporalAdjusters.nextOrSame(selectedDay));

                // Ensure that the occurrence falls within the date range
                if (!nextOccurrence.isAfter(endDate) && !nextOccurrence.isBefore(body.getMatchDate())) {
                    totalPrice += timeSlotPrice;  // Add the price for each valid day
                }
            }
            // Move to the next week
            startDate = startDate.plusWeeks(1);
        }

        // Apply a 20% discount for weekly bookings
//        totalPrice = totalPrice * 0.80;
        return totalPrice;
    }

    // Calculate the total price for monthly bookings
    private double calculateMonthlyPrice(BookingRequest body, double timeSlotPrice) {
        double totalPrice = 0.0;
        LocalDate startDate = body.getMatchDate();
        LocalDate endDate = body.getRecurrenceEndDate();
        List<DayOfWeek> recurrenceDays = body.getRecurrenceDays();

        // Loop through each month between startDate and endDate
        while (!startDate.isAfter(endDate)) {
            YearMonth currentMonth = YearMonth.from(startDate);
            LocalDate firstDayOfMonth = currentMonth.atDay(1);
            LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();

            // Loop through each recurrence day in the month
            for (DayOfWeek selectedDay : recurrenceDays) {
                LocalDate currentDay = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(selectedDay));

                // Loop through all occurrences of the selected day within the current month
                while (!currentDay.isAfter(lastDayOfMonth) && !currentDay.isAfter(endDate)) {
                    if (!currentDay.isBefore(startDate)) {
                        totalPrice += timeSlotPrice;  // Add the price for each valid occurrence
                    }
                    currentDay = currentDay.plusWeeks(1);  // Move to the next occurrence of the same day in the month
                }
            }

            // Move to the next month
            startDate = startDate.plusMonths(1).withDayOfMonth(1);
        }

        // Apply a 30% discount for monthly bookings
        totalPrice *= 0.70;

        return totalPrice;
    }


    @GetMapping("/one-time")
    public List<BookedVenueResponseDTO> getOneTimeBookings(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
    ) throws CommonException {
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String phoneNumber = jwtService.extractUsername(token);
        return bookingService.getOneTimeBookings(phoneNumber, page, size);
    }

    @GetMapping("/recurring")
    public List<Map<String, Object>> getRecurringBookings(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10000", required = false) int size
    ) throws CommonException {
        String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
        String phoneNumber = jwtService.extractUsername(token);
        return bookingService.getRecurringBookings(phoneNumber, page, size);
    }

}
