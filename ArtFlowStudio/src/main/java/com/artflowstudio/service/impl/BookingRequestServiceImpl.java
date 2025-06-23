package com.artflowstudio.service.impl;

import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.entity.BookingRequest;
import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.enums.BookingStatus;
import com.artflowstudio.exception.ClassFullException; // Sẽ tạo sau
import com.artflowstudio.exception.ResourceNotFoundException; // Sẽ tạo sau
import com.artflowstudio.repository.BookingRequestRepository;
import com.artflowstudio.repository.ClassScheduleRepository;
import com.artflowstudio.service.BookingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingRequestServiceImpl implements BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final ClassScheduleRepository classScheduleRepository;
    // EmailService sẽ inject sau

    @Autowired
    public BookingRequestServiceImpl(BookingRequestRepository bookingRequestRepository,
                                     ClassScheduleRepository classScheduleRepository) {
        this.bookingRequestRepository = bookingRequestRepository;
        this.classScheduleRepository = classScheduleRepository;
    }

    @Override
    @Transactional
    public BookingRequest createBookingRequest(BookingRequestDto dto, Long classScheduleId) {
        ClassSchedule classSchedule = classScheduleRepository.findById(classScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với ID: " + classScheduleId));

        if (classSchedule.getAvailableSlots() <= 0) {
            throw new ClassFullException("Lớp học '" + classSchedule.getCourse().getName() + "' đã đầy chỗ.");
        }

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setFullName(dto.getFullName());
        bookingRequest.setAge(dto.getAge());
        bookingRequest.setEmail(dto.getEmail());
        bookingRequest.setPhoneNumber(dto.getPhoneNumber());
        bookingRequest.setMessage(dto.getMessage());
        bookingRequest.setClassSchedule(classSchedule);
        bookingRequest.setStatus(BookingStatus.PENDING); // Trạng thái chờ duyệt

        return bookingRequestRepository.save(bookingRequest);
    }
}