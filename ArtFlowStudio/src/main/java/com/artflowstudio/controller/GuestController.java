package com.artflowstudio.controller;

import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.entity.Course;
import com.artflowstudio.exception.ClassFullException;
import com.artflowstudio.exception.CourseNotFoundException; // Sẽ tạo sau
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.service.ClassScheduleService;
import com.artflowstudio.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.artflowstudio.dto.BookingRequestDto;
import com.artflowstudio.service.BookingRequestService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class GuestController {

    private final CourseService courseService;
    private final ClassScheduleService classScheduleService;
    private final BookingRequestService bookingRequestService;

    @Autowired
    public GuestController(CourseService courseService, ClassScheduleService classScheduleService,
                           BookingRequestService bookingRequestService) {
        this.courseService = courseService;
        this.classScheduleService = classScheduleService;
        this.bookingRequestService = bookingRequestService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("pageTitle", "Trang Chủ ArtFlow Studio");
        return "index";
    }

    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Danh sách Khóa học");
        return "guest/list-courses"; // Sẽ tạo view này
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        Course course = courseService.findCourseById(id)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học với ID: " + id));

        List<ClassSchedule> schedules = classScheduleService.findSchedulesByCourseId(id);

        model.addAttribute("course", course);
        model.addAttribute("schedules", schedules); // Lịch học của khóa này
        model.addAttribute("pageTitle", course.getName());
        return "guest/course-detail"; // Sẽ tạo view này
    }

    @GetMapping("/schedule")
    public String scheduleOverview(Model model) {
        List<ClassSchedule> schedules = classScheduleService.findAllSchedules(); // Đã sửa trong service để lấy upcoming
        model.addAttribute("schedules", schedules);
        model.addAttribute("pageTitle", "Lịch học Tổng quan");
        return "guest/schedule-overview"; // Sẽ tạo view này
    }

    @GetMapping("/book/{classScheduleId}")
    public String showBookingForm(@PathVariable Long classScheduleId, Model model, RedirectAttributes redirectAttributes) {
        ClassSchedule classSchedule = classScheduleService.findScheduleById(classScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại."));

        if (classSchedule.getAvailableSlots() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rất tiếc, lớp học này đã đủ số lượng học viên.");
            return "redirect:/courses/" + classSchedule.getCourse().getId();
        }

        model.addAttribute("classSchedule", classSchedule);
        model.addAttribute("bookingRequestDto", new BookingRequestDto());
        model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName());
        return "guest/book-form"; // Sẽ tạo view này
    }

    @PostMapping("/book")
    public String processBooking(@Valid @ModelAttribute("bookingRequestDto") BookingRequestDto bookingRequestDto,
                                 BindingResult result,
                                 @RequestParam("classScheduleId") Long classScheduleId, // Lấy từ hidden input
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        ClassSchedule classSchedule = classScheduleService.findScheduleById(classScheduleId)
                .orElse(null); // Tìm lại classSchedule để hiển thị nếu có lỗi

        if (classSchedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp học không hợp lệ.");
            return "redirect:/courses";
        }

        if (classSchedule.getAvailableSlots() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lớp đã hết chỗ trong khi bạn thao tác.");
            return "redirect:/courses/" + classSchedule.getCourse().getId();
        }

        if (result.hasErrors()) {
            model.addAttribute("classSchedule", classSchedule); // Gửi lại để hiển thị thông tin lớp học
            model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName() + " - Lỗi");
            return "guest/book-form";
        }

        try {
            bookingRequestService.createBookingRequest(bookingRequestDto, classScheduleId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Yêu cầu đặt chỗ của bạn cho lớp '" + classSchedule.getCourse().getName() + "' đã được gửi thành công! Chúng tôi sẽ liên hệ với bạn sớm.");
            return "redirect:/courses"; // Hoặc redirect tới trang cảm ơn
        } catch (ClassFullException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses/" + classSchedule.getCourse().getId();
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/courses";
        } catch (Exception e) {
            // Log lỗi ở đây
            model.addAttribute("classSchedule", classSchedule);
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đặt chỗ. Vui lòng thử lại.");
            model.addAttribute("pageTitle", "Đặt chỗ lớp: " + classSchedule.getCourse().getName() + " - Lỗi");
            return "guest/book-form";
        }
    }
}