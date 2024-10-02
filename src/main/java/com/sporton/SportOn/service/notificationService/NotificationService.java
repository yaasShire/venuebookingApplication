package com.sporton.SportOn.service.notificationService;

import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPaymentReminder(AppUser user, Booking booking) {
        String subject = "Payment Due Reminder";
        String body = "Dear " + user.getFullName() + ",\n\nYour payment for your recurring booking is due on " +
                booking.getPaymentDueDate() + ". Please make your payment to continue enjoying the service.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendBookingCancellation(AppUser user, Booking booking) {
        String subject = "Booking Cancellation";
        String body = "Dear " + user.getFullName() + ",\n\nYour Booking Is Being Cancelled Due To Payment " +
                booking.getPaymentDueDate() + ". Please make your payment to continue enjoying the service.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
