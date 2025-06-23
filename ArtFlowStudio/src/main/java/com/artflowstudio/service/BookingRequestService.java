package com.artflowstudio.service;

import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.entity.BookingRequest;
 import com.artflowstudio.enums.BookingStatus;
 import java.util.List;
 import java.util.Optional;

public interface BookingRequestService {
    BookingRequest createBookingRequest(BookingRequestDto bookingRequestDto, Long classScheduleId);
    // Các phương thức cho Admin (findByStatus, approve, reject) sẽ thêm sau
}