package com.artflowstudio.service;

import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.entity.BookingRequest;
import com.artflowstudio.enums.BookingStatus;
import java.util.List;
import java.util.Optional;

public interface BookingRequestService {
    BookingRequest createBookingRequest(BookingRequestDto bookingRequestDto, Long classScheduleId);

    List<BookingRequest> findByStatus(BookingStatus status);
    Optional<BookingRequest> findById(Long id);
    BookingRequest approveBooking(Long bookingRequestId) throws Exception;
    BookingRequest rejectBooking(Long bookingRequestId) throws Exception;
}