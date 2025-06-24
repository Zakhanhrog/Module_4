package com.artflowstudio.controller;

import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.entity.Course;
import com.artflowstudio.entity.User;
import com.artflowstudio.exception.CourseNotFoundException;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.service.BookingRequestService;
import com.artflowstudio.service.ClassScheduleService;
import com.artflowstudio.service.CourseService;
import com.artflowstudio.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class GuestController {

    private final CourseService courseService;
    private final ClassScheduleService classScheduleService;
    private final BookingRequestService bookingRequestService;
    private final UserService userService;

    @Autowired
    public GuestController(CourseService courseService,
                           ClassScheduleService classScheduleService,
                           BookingRequestService bookingRequestService,
                           UserService userService) {
        this.courseService = courseService;
        this.classScheduleService = classScheduleService;
        this.bookingRequestService = bookingRequestService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        List<Course> allCourses = courseService.findAllCourses();
        List<Course> featuredCourses = allCourses.stream().limit(3).collect(Collectors.toList());

        model.addAttribute("featuredCourses", featuredCourses);
        model.addAttribute("pageTitle", "ArtFlow Studio - Khơi Nguồn Sáng Tạo");
        return "index";
    }

    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Danh sách Khóa học");
        return "guest/list-courses";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseService.findCourseById(id)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học với ID: " + id));
        List<ClassSchedule> schedules = classScheduleService.findSchedulesByCourseId(id);
        model.addAttribute("course", course);
        model.addAttribute("schedules", schedules);
        model.addAttribute("pageTitle", course.getName());
        return "guest/course-detail";
    }

    @GetMapping("/schedule")
    public String scheduleOverview(Model model) {
        List<ClassSchedule> schedules = classScheduleService.findAllSchedules();
        model.addAttribute("schedules", schedules);
        model.addAttribute("pageTitle", "Lịch học Tổng quan");
        return "guest/schedule-overview";
    }

    @GetMapping("/book/{classScheduleId}")
    public String showBookingForm(@PathVariable Long classScheduleId, Model model, RedirectAttributes redirectAttributes) {
        ClassSchedule classSchedule = classScheduleService.findScheduleById(classScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại."));

        if (classSchedule.getAvailableSlots() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rất tiếc, lớp học này đã đủ số lượng học viên.");
            return "redirect:/courses/" + classSchedule.getCourse().getId();
        }

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isUserAuthenticatedAndLearner = false;

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            if (authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_LEARNER"))) {
                String username = authentication.getName();
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User currentUser = userOpt.get();
                    bookingRequestDto.setFullName(currentUser.getFullName());
                    bookingRequestDto.setEmail(currentUser.getUsername());
                    bookingRequestDto.setPhoneNumber(currentUser.getPhoneNumber());
                    bookingRequestDto.setAge(currentUser.getAge());
                    isUserAuthenticatedAndLearner = true;
                }
            }
        }
        model.addAttribute("isLearnerLoggedIn", isUserAuthenticatedAndLearner);
        model.addAttribute("classSchedule", classSchedule);
        model.addAttribute("bookingRequestDto", bookingRequestDto);
        model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName());
        return "guest/book-form";
    }

    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("bookingRequestDto") BookingRequestDto bookingRequestDto,
                                 BindingResult result,
                                 @RequestParam("classScheduleId") Long classScheduleId,
                                 Model model, RedirectAttributes redirectAttributes) {

        ClassSchedule classSchedule = classScheduleService.findScheduleById(classScheduleId)
                .orElse(null);

        if (classSchedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học không hợp lệ.");
            return "redirect:/courses";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isUserAuthenticatedAndLearner = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            if (authentication.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_LEARNER"))) {
                isUserAuthenticatedAndLearner = true;
            }
        }

        if (isUserAuthenticatedAndLearner) {
            User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
            if(currentUser != null && !bookingRequestDto.getEmail().equalsIgnoreCase(currentUser.getUsername())){
                result.rejectValue("email", "error.bookingRequestDto", "Email phải khớp với tài khoản đang đăng nhập của bạn.");
            }
        }


        if (result.hasErrors()) {
            model.addAttribute("classSchedule", classSchedule);
            model.addAttribute("isLearnerLoggedIn", isUserAuthenticatedAndLearner);
            model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName() + " - Lỗi");
            return "guest/book-form";
        }

        if (classSchedule.getAvailableSlots() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp đã hết chỗ trong khi bạn thao tác.");
            return "redirect:/courses/" + classSchedule.getCourse().getId();
        }

        try {
            bookingRequestService.createBookingRequest(bookingRequestDto, classScheduleId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Yêu cầu đặt chỗ của bạn cho lớp '" + classSchedule.getCourse().getName() + "' đã được gửi thành công! Chúng tôi sẽ liên hệ với bạn sớm.");
            return "redirect:/courses";
        } catch (Exception e) {
            model.addAttribute("classSchedule", classSchedule);
            model.addAttribute("isLearnerLoggedIn", isUserAuthenticatedAndLearner);
            model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName() + " - Lỗi");
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đặt chỗ: " + e.getMessage());
            return "guest/book-form";
        }
    }


}