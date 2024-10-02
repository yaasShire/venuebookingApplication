package com.sporton.SportOn.service.paymentReminderService;

import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Booking;
import com.sporton.SportOn.repository.AppUserRepository;
import com.sporton.SportOn.repository.BookingRepository;
import com.sporton.SportOn.service.notificationService.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    @Autowired
    private BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;

    @Autowired
    private NotificationService notificationService; // Assume this service sends notifications

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9:00 AM
    public void checkForUpcomingPayments() {
        LocalDate currentDate = LocalDate.now();
        List<Booking> upcomingPayments = bookingRepository.findBookingsWithUpcomingPayments(currentDate.plusDays(5)); // 5 days before due date

        for (Booking booking : upcomingPayments) {
            Long userId = booking.getUserId();
            Optional<AppUser> user = appUserRepository.findAppUserById(userId);
            notificationService.sendPaymentReminder(user.get(), booking);
        }
    }
}
