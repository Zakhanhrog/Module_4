package com.artflowstudio.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    // void sendBookingConfirmationEmail(User user, BookingRequest bookingRequest, String generatedPassword);
    // void sendBookingRejectionEmail(BookingRequest bookingRequest);
}