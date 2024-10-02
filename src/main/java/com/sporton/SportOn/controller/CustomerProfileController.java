package com.sporton.SportOn.controller;

import com.sporton.SportOn.dto.CustomerProfileDTO;
import com.sporton.SportOn.dto.ReturningCustomerDTO;
import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Booking;
import com.sporton.SportOn.repository.AppUserRepository;
import com.sporton.SportOn.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerProfileController {
    private final BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/profiles")
    public List<CustomerProfileDTO> getCustomerProfiles(
            @RequestParam(value = "venueId", required = false) Long venueId) {

        List<Booking> bookings;

        // Get bookings for a specific venue if venueId is provided, otherwise get all bookings
        if (venueId != null) {
            bookings = bookingRepository.findByVenueId(venueId);
        } else {
            bookings = bookingRepository.findAll();
        }

        // Extract unique user IDs from the bookings
        Set<Long> userIds = bookings.stream()
                .map(Booking::getUserId) // Using getUserId method from Booking entity
                .collect(Collectors.toSet());

        // Fetch users based on the extracted userIds
        List<AppUser> users = appUserRepository.findAllById(userIds);

        // Prepare customer profiles
        return users.stream()
                .map(this::mapToCustomerProfileDTO)
                .collect(Collectors.toList());
    }


    @GetMapping("/returning-customers-percentage")
    public ReturningCustomerDTO getReturningCustomersPercentage(
            @RequestParam(value = "venueId", required = false) Long venueId) {

        List<Booking> bookings;

        // Get bookings for a specific venue if venueId is provided, otherwise get all bookings
        if (venueId != null) {
            bookings = bookingRepository.findByVenueId(venueId);
        } else {
            bookings = bookingRepository.findAll();
        }

        // Group bookings by userId to identify unique customers
        Map<Long, List<Booking>> bookingsByUser = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getUserId));

        int totalBookings = bookings.size();
        int returningCustomerBookings = 0;

        // Iterate over grouped bookings to classify new and returning customers
        for (List<Booking> userBookings : bookingsByUser.values()) {
            if (userBookings.size() > 1) {
                // If the user has made more than one booking, consider them a returning customer
                returningCustomerBookings += userBookings.size();
            }
        }

        // Calculate percentage for returning and new customers
        int newCustomerBookings = totalBookings - returningCustomerBookings;
        double returningCustomerPercentage = (double) returningCustomerBookings / totalBookings * 100;
        double newCustomerPercentage = (double) newCustomerBookings / totalBookings * 100;

        // Prepare and return the result
        ReturningCustomerDTO response = new ReturningCustomerDTO();
        response.setTotalBookings(totalBookings);
        response.setReturningCustomerBookings(returningCustomerBookings);
        response.setNewCustomerBookings(newCustomerBookings);
        response.setReturningCustomerPercentage(returningCustomerPercentage);
        response.setNewCustomerPercentage(newCustomerPercentage);

        return response;
    }



    // Helper function to convert AppUser to a DTO containing demographic info
    private CustomerProfileDTO mapToCustomerProfileDTO(AppUser user) {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setCity(user.getCity());
        dto.setGender(user.getGender());

        // Calculate age based on dateOfBirth
        if (user.getDateOfBirth() != null) {
            LocalDate birthDate = LocalDate.parse(user.getDateOfBirth()); // Assuming the date is in YYYY-MM-DD format
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            dto.setAge(age);

            // Categorize into age groups
            if (age >= 18 && age <= 25) {
                dto.setAgeGroup("18-25");
            } else if (age >= 26 && age <= 35) {
                dto.setAgeGroup("26-35");
            } else if (age >= 36 && age <= 45) {
                dto.setAgeGroup("36-45");
            } else {
                dto.setAgeGroup("46+");
            }
        }

        return dto;
    }
}
