package com.sporton.SportOn.service.bookingService;

import com.sporton.SportOn.entity.Booking;
import com.sporton.SportOn.exception.commonException.CommonException;
import com.sporton.SportOn.exception.venueException.VenueException;
import com.sporton.SportOn.model.CommonResponseModel;
import com.sporton.SportOn.model.bookingModel.BookedVenueResponseDTO;
import com.sporton.SportOn.model.bookingModel.BookingRequest;
import com.sporton.SportOn.model.bookingModel.MatchesRequestModel;
import com.sporton.SportOn.model.bookingModel.ProviderOrderResponseDTO;
import com.sporton.SportOn.service.venueService.VenueRevenueResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    CommonResponseModel bookCourt(BookingRequest body, String phoneNumber) throws CommonException;

    Map<String, Object> getBookingsByUserId(String phoneNumber , Integer page, Integer size) throws CommonException;

    List<Object> getBookingByCustomerId(String phoneNumber, Integer page, Integer size) throws CommonException;

    CommonResponseModel updateBooking(Long bookingId, BookingRequest body) throws CommonException;

    CommonResponseModel cancelBooking(Long bookingId) throws CommonException;

    CommonResponseModel confirmtBooking(Long bookingId) throws CommonException;

    List<ProviderOrderResponseDTO> getBookingsByProviderId(String phoneNumber , Integer page, Integer size) throws CommonException;


    CommonResponseModel getNumberOfNewOrders(String phoneNumber, Integer page, Integer size) throws CommonException;

    CommonResponseModel getPendingOrders(String phoneNumber, Integer page, Integer size) throws CommonException;
    CommonResponseModel getExpiredOrders(String phoneNumber, Integer page, Integer size) throws CommonException;
    CommonResponseModel getCompletedOrders(String phoneNumber, Integer page, Integer size) throws CommonException;


    CommonResponseModel getConfirmedOrders(String phoneNumber , Integer page, Integer size) throws CommonException;

    CommonResponseModel getCancelledOrders(String phoneNumber , Integer page, Integer size) throws CommonException;

    CommonResponseModel getNumberOfTodayOrders(String phoneNumber , Integer page, Integer size);

    CommonResponseModel getNumberOfTodayMatches(String phoneNumber , Integer page, Integer size);

    CommonResponseModel getNumberOfPendingOrders(String phoneNumber , Integer page, Integer size);

    CommonResponseModel getMatchesByDate(String phoneNumber, MatchesRequestModel body , Integer page, Integer size) throws CommonException;

    CommonResponseModel getTotalIncomeByPeriod(String periodType);

    CommonResponseModel getLast12MonthsIncome(String phoneNumber) throws CommonException;

    CommonResponseModel getTotalIncomeForProvider(String phoneNumber) throws CommonException;

    CommonResponseModel getTotalVenueIncome(Long venueId, String period);

    CommonResponseModel getTotalIncomeOfTodayOrders(String phoneNumber, int page, int size);

    CommonResponseModel getTotalBookings(Long venueId, LocalDate start, LocalDate end) throws CommonException;

    CommonResponseModel getTotalRevenue(Long venueId, LocalDate start, LocalDate end) throws VenueException;

    void createOneTimeBooking(Booking booking);


    VenueRevenueResult getVenueWithHighestRevenue(String startDate, String endDate) throws CommonException;

    List<BookedVenueResponseDTO> getOneTimeBookings(String phoneNumber, int page, int size) throws CommonException;

    List<Map<String, Object>> getRecurringBookings(String phoneNumber, int page, int size) throws CommonException;
}
