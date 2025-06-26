package com.artflowstudio.service.impl;

import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.entity.BookingRequest;
import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.entity.Enrollment;
import com.artflowstudio.entity.User;
import com.artflowstudio.enums.BookingStatus;
import com.artflowstudio.enums.Role;
import com.artflowstudio.exception.ClassFullException;
import com.artflowstudio.exception.InvalidOperationException;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.repository.BookingRequestRepository;
import com.artflowstudio.repository.ClassScheduleRepository;
import com.artflowstudio.repository.EnrollmentRepository;
import com.artflowstudio.repository.UserRepository;
import com.artflowstudio.service.BookingRequestService;
import com.artflowstudio.service.EmailService;
import com.artflowstudio.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingRequestServiceImpl implements BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public BookingRequestServiceImpl(BookingRequestRepository bookingRequestRepository,
                                     ClassScheduleRepository classScheduleRepository,
                                     UserRepository userRepository,
                                     EnrollmentRepository enrollmentRepository,
                                     PasswordEncoder passwordEncoder,
                                     EmailService emailService,
                                     UserService userService) {
        this.bookingRequestRepository = bookingRequestRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.userService = userService;
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
        bookingRequest.setStatus(BookingStatus.PENDING);

        return bookingRequestRepository.save(bookingRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingRequest> findByStatus(BookingStatus status) {
        return bookingRequestRepository.findByStatusWithDetails(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookingRequest> findById(Long id) {
        return bookingRequestRepository.findById(id);
    }

    @Override
    @Transactional
    public BookingRequest approveBooking(Long bookingRequestId) throws Exception {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu đăng ký không tồn tại với ID: " + bookingRequestId));

        if (bookingRequest.getStatus() != BookingStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể duyệt các đăng ký đang ở trạng thái 'Chờ duyệt'.");
        }

        ClassSchedule classSchedule = bookingRequest.getClassSchedule();
        Optional<User> existingUserOpt = userService.findByUsername(bookingRequest.getEmail());
        User learner;
        String generatedPassword = null;

        if (existingUserOpt.isEmpty()) {
            learner = new User();
            learner.setUsername(bookingRequest.getEmail());
            generatedPassword = UUID.randomUUID().toString().substring(0, 8);
            learner.setPassword(passwordEncoder.encode(generatedPassword));
            learner.setFullName(bookingRequest.getFullName());
            learner.setAge(bookingRequest.getAge());
            learner.setPhoneNumber(bookingRequest.getPhoneNumber());
            Set<Role> roles = new HashSet<>();
            roles.add(Role.LEARNER);
            learner.setRoles(roles);
            learner.setEnabled(true);
            userService.saveUser(learner);
        } else {
            learner = existingUserOpt.get();
            if (!learner.getRoles().contains(Role.LEARNER)) {
                learner.getRoles().add(Role.LEARNER);
                userService.saveUser(learner);
            }
        }

        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByUserAndClassScheduleId(learner, classSchedule.getId());
        if (existingEnrollment.isPresent()) {
            bookingRequest.setStatus(BookingStatus.APPROVED);
            bookingRequestRepository.save(bookingRequest);
            throw new InvalidOperationException("Học viên '" + learner.getFullName() + "' đã được ghi danh vào lớp học '" + classSchedule.getCourse().getName() + " (ID: " + classSchedule.getId() + ")' này trước đó.");
        }

        if (classSchedule.getAvailableSlots() <= 0) {
            bookingRequest.setStatus(BookingStatus.REJECTED);
            bookingRequestRepository.save(bookingRequest);
            emailService.sendSimpleMessage(
                    bookingRequest.getEmail(),
                    "ArtFlow Studio - Đăng ký Lớp học bị Từ chối do Hết chỗ",
                    "Chào " + bookingRequest.getFullName() + ",\n\n" +
                            "Rất tiếc, yêu cầu đăng ký lớp học '" + classSchedule.getCourse().getName() +
                            "' của bạn đã bị từ chối do lớp học đã đủ số lượng học viên khi chúng tôi xử lý yêu cầu của bạn.\n\n" +
                            "Vui lòng theo dõi các lớp học khác của chúng tôi.\n\n" +
                            "Trân trọng,\nArtFlow Studio"
            );
            throw new ClassFullException("Lớp học '" + classSchedule.getCourse().getName() + "' đã đầy chỗ khi duyệt đăng ký.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(learner);
        enrollment.setClassSchedule(classSchedule);
        enrollmentRepository.save(enrollment);

        classSchedule.setCurrentStudents(classSchedule.getCurrentStudents() + 1);
        classScheduleRepository.save(classSchedule);

        bookingRequest.setStatus(BookingStatus.APPROVED);
        BookingRequest approvedRequest = bookingRequestRepository.save(bookingRequest);

        String emailSubject = "ArtFlow Studio - Đăng ký Lớp học Thành công!";
        String emailText = "Chào " + learner.getFullName() + ",\n\n" +
                "Yêu cầu đăng ký lớp học '" + classSchedule.getCourse().getName() +
                "' (Khai giảng: " + classSchedule.getStartDate().toString() + ")" +
                " của bạn đã được CHẤP NHẬN.\n\n";

        if (generatedPassword != null) {
            emailText += "Một tài khoản học viên đã được tạo cho bạn với thông tin sau:\n" +
                    "Tên đăng nhập (Email): " + learner.getUsername() + "\n" +
                    "Mật khẩu tạm thời: " + generatedPassword + "\n" +
                    "Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu tiên.\n\n";
        } else {
            emailText += "Bạn có thể đăng nhập bằng tài khoản hiện tại của mình để xem chi tiết.\n\n";
        }
        emailText += "Trân trọng,\nArtFlow Studio";
        emailService.sendSimpleMessage(learner.getUsername(), emailSubject, emailText);

        return approvedRequest;
    }

    @Override
    @Transactional
    public BookingRequest rejectBooking(Long bookingRequestId) throws Exception {
        BookingRequest bookingRequest = bookingRequestRepository.findById(bookingRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu đăng ký không tồn tại với ID: " + bookingRequestId));

        if (bookingRequest.getStatus() != BookingStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể từ chối các đăng ký đang ở trạng thái 'Chờ duyệt'.");
        }

        bookingRequest.setStatus(BookingStatus.REJECTED);
        BookingRequest rejectedRequest = bookingRequestRepository.save(bookingRequest);

        String emailSubject = "ArtFlow Studio - Thông báo về Yêu cầu Đăng ký Lớp học";
        String emailText = "Chào " + bookingRequest.getFullName() + ",\n\n" +
                "Chúng tôi rất tiếc phải thông báo rằng yêu cầu đăng ký của bạn cho lớp học '" +
                bookingRequest.getClassSchedule().getCourse().getName() +
                "' đã không được chấp thuận vào thời điểm này.\n\n" +
                "Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.\n\n" +
                "Trân trọng,\nArtFlow Studio";
        emailService.sendSimpleMessage(bookingRequest.getEmail(), emailSubject, emailText);

        return rejectedRequest;
    }
}