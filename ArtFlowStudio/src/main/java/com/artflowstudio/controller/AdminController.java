package com.artflowstudio.controller;

import com.artflowstudio.dto.ClassScheduleDto;
import com.artflowstudio.dto.GradeDto;
import com.artflowstudio.entity.*;
import com.artflowstudio.enums.BookingStatus;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CourseService courseService;
    private final ClassScheduleService classScheduleService; // Thêm vào
    private final InstructorService instructorService;
    private final BookingRequestService bookingRequestService;
    private final UserService userService;
    private final EnrollmentService enrollmentService; // Thêm
    private final GradeService gradeService;

    @Autowired
    public AdminController(CourseService courseService,
                           ClassScheduleService classScheduleService,
                           InstructorService instructorService,
                           BookingRequestService bookingRequestService,
                           UserService userService,
                           EnrollmentService enrollmentService,
                           GradeService gradeService) {
        this.courseService = courseService;
        this.classScheduleService = classScheduleService;
        this.instructorService = instructorService;
        this.bookingRequestService = bookingRequestService;
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.gradeService = gradeService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Quản lý Khóa học");
        return "admin/courses/list";
    }

    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("pageTitle", "Thêm Khóa học Mới");
        return "admin/courses/form";
    }

    @PostMapping("/courses/save")
    public String saveCourse(@Valid @ModelAttribute("course") Course course,
                             BindingResult result,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", (course.getId() == null ? "Thêm" : "Sửa") + " Khóa học - Lỗi");
            return "admin/courses/form";
        }
        try {
            courseService.saveCourse(course, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Khóa học đã được lưu thành công!");
        } catch (IOException e) {
            // Log lỗi e.printStackTrace();
            model.addAttribute("pageTitle", (course.getId() == null ? "Thêm" : "Sửa") + " Khóa học - Lỗi");
            model.addAttribute("errorMessage", "Lỗi xử lý file ảnh: " + e.getMessage());
            return "admin/courses/form";
        } catch (Exception e) {
            // Log lỗi e.printStackTrace();
            model.addAttribute("pageTitle", (course.getId() == null ? "Thêm" : "Sửa") + " Khóa học - Lỗi");
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra khi lưu khóa học: " + e.getMessage());
            return "admin/courses/form";
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String editCourseForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Course> courseOpt = courseService.findCourseById(id);
        if (courseOpt.isPresent()) {
            model.addAttribute("course", courseOpt.get());
            model.addAttribute("pageTitle", "Sửa Khóa học: " + courseOpt.get().getName());
            return "admin/courses/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khóa học với ID: " + id);
            return "redirect:/admin/courses";
        }
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khóa học đã được xóa thành công.");
        } catch (Exception e) {
            // Log lỗi e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa khóa học: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/class-schedules")
    public String listClassSchedules(Model model) {
        List<ClassSchedule> schedules = classScheduleService.findAllSchedulesForAdmin(); // Lấy tất cả lịch
        model.addAttribute("schedules", schedules);
        model.addAttribute("pageTitle", "Quản lý Lớp học");
        return "admin/class-schedules/list"; // Sẽ tạo view này
    }

    @GetMapping("/class-schedules/new")
    public String newClassScheduleForm(Model model) {
        model.addAttribute("classScheduleDto", new ClassScheduleDto());
        model.addAttribute("courses", courseService.findAllCourses()); // Để chọn khóa học
        model.addAttribute("instructors", instructorService.findAllInstructors()); // Để chọn giảng viên (sẽ làm InstructorService sau)
        model.addAttribute("pageTitle", "Thêm Lớp học Mới");
        return "admin/class-schedules/form"; // Sẽ tạo view này
    }

    @PostMapping("/class-schedules/save")
    public String saveClassSchedule(@Valid @ModelAttribute("classScheduleDto") ClassScheduleDto classScheduleDto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes, Model model) {
        if (classScheduleDto.getStartTime() != null && classScheduleDto.getEndTime() != null &&
                classScheduleDto.getStartTime().isAfter(classScheduleDto.getEndTime())) {
            result.rejectValue("endTime", "error.classScheduleDto", "Giờ kết thúc phải sau giờ bắt đầu.");
        }

        if (result.hasErrors()) {
            model.addAttribute("courses", courseService.findAllCourses());
            model.addAttribute("instructors", instructorService.findAllInstructors());
            model.addAttribute("pageTitle", (classScheduleDto.getId() == null ? "Thêm" : "Sửa") + " Lớp học - Lỗi");
            return "admin/class-schedules/form";
        }
        try {
            if (classScheduleDto.getId() == null) {
                classScheduleService.saveSchedule(classScheduleDto);
                redirectAttributes.addFlashAttribute("successMessage", "Lớp học đã được thêm thành công!");
            } else {
                classScheduleService.updateSchedule(classScheduleDto.getId(), classScheduleDto);
                redirectAttributes.addFlashAttribute("successMessage", "Lớp học đã được cập nhật thành công!");
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // Log e.printStackTrace();
            model.addAttribute("courses", courseService.findAllCourses());
            model.addAttribute("instructors", instructorService.findAllInstructors());
            model.addAttribute("pageTitle", (classScheduleDto.getId() == null ? "Thêm" : "Sửa") + " Lớp học - Lỗi");
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra: " + e.getMessage());
            return "admin/class-schedules/form";
        }
        return "redirect:/admin/class-schedules";
    }

    @GetMapping("/class-schedules/edit/{id}")
    public String editClassScheduleForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ClassSchedule> scheduleOpt = classScheduleService.findScheduleById(id);
        if (scheduleOpt.isPresent()) {
            ClassSchedule schedule = scheduleOpt.get();
            ClassScheduleDto dto = new ClassScheduleDto();
            dto.setId(schedule.getId());
            dto.setCourseId(schedule.getCourse().getId());
            if (schedule.getInstructor() != null) {
                dto.setInstructorId(schedule.getInstructor().getId());
            }
            dto.setStartDate(schedule.getStartDate());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            dto.setDaysOfWeek(schedule.getDaysOfWeek());
            dto.setNumberOfSessions(schedule.getNumberOfSessions());
            dto.setMaxStudents(schedule.getMaxStudents());
            dto.setCurrentStudents(schedule.getCurrentStudents());

            model.addAttribute("classScheduleDto", dto);
            model.addAttribute("courses", courseService.findAllCourses());
            model.addAttribute("instructors", instructorService.findAllInstructors());
            model.addAttribute("pageTitle", "Sửa Lớp học");
            return "admin/class-schedules/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học với ID: " + id);
            return "redirect:/admin/class-schedules";
        }
    }

    @GetMapping("/class-schedules/delete/{id}")
    public String deleteClassSchedule(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            classScheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lớp học đã được xóa thành công.");
        } catch (Exception e) {
            // Log e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa lớp học: " + e.getMessage());
        }
        return "redirect:/admin/class-schedules";
    }

    @GetMapping("/instructors")
    public String listInstructors(Model model) {
        model.addAttribute("instructors", instructorService.findAllInstructors());
        model.addAttribute("pageTitle", "Quản lý Giảng viên");
        return "admin/instructors/list";
    }

    @GetMapping("/instructors/new")
    public String newInstructorForm(Model model) {
        model.addAttribute("instructor", new Instructor());
        model.addAttribute("pageTitle", "Thêm Giảng viên Mới");
        return "admin/instructors/form";
    }

    @PostMapping("/instructors/save")
    public String saveInstructor(@Valid @ModelAttribute("instructor") Instructor instructor,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", (instructor.getId() == null ? "Thêm" : "Sửa") + " Giảng viên - Lỗi");
            return "admin/instructors/form";
        }
        try {
            instructorService.saveInstructor(instructor);
            redirectAttributes.addFlashAttribute("successMessage", "Giảng viên đã được lưu thành công!");
        } catch (Exception e) {
            model.addAttribute("pageTitle", (instructor.getId() == null ? "Thêm" : "Sửa") + " Giảng viên - Lỗi");
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra: " + e.getMessage());
            return "admin/instructors/form";
        }
        return "redirect:/admin/instructors";
    }

    @GetMapping("/instructors/edit/{id}")
    public String editInstructorForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Instructor> instructorOpt = instructorService.findById(id);
        if (instructorOpt.isPresent()) {
            model.addAttribute("instructor", instructorOpt.get());
            model.addAttribute("pageTitle", "Sửa Giảng viên: " + instructorOpt.get().getName());
            return "admin/instructors/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giảng viên với ID: " + id);
            return "redirect:/admin/instructors";
        }
    }

    @GetMapping("/instructors/delete/{id}")
    public String deleteInstructor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            instructorService.deleteInstructor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Giảng viên đã được xóa thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa giảng viên: " + e.getMessage() + ". Có thể giảng viên đang được gán cho một lớp học.");
        }
        return "redirect:/admin/instructors";
    }

    @GetMapping("/booking-requests")
    public String listPendingBookingRequests(Model model) {
        List<BookingRequest> pendingRequests = bookingRequestService.findByStatus(BookingStatus.PENDING);
        model.addAttribute("bookingRequests", pendingRequests);
        model.addAttribute("pageTitle", "Duyệt Đăng ký Học viên");
        return "admin/booking-requests/list";
    }

    @PostMapping("/booking-requests/approve/{id}")
    public String approveBookingRequest(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingRequestService.approveBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đã được duyệt thành công và email đã được gửi.");
        } catch (Exception e) {
            // Log e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi duyệt đăng ký: " + e.getMessage());
        }
        return "redirect:/admin/booking-requests";
    }

    @PostMapping("/booking-requests/reject/{id}")
    public String rejectBookingRequest(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingRequestService.rejectBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký đã được từ chối.");
        } catch (Exception e) {
                e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi từ chối đăng ký: " + e.getMessage());
        }
        return "redirect:/admin/booking-requests";
    }

    @GetMapping("/learners")
    public String listLearners(Model model) {
        model.addAttribute("learners", userService.findAllLearners());
        model.addAttribute("pageTitle", "Quản lý Học viên");
        return "admin/learners/list";
    }

    @GetMapping("/learners/view/{id}")
    public String viewLearner(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("ADMIN CONTROLLER - viewLearner - Received learner ID: " + id); // LOG 1
        Optional<User> learnerOpt = userService.findById(id);

        if (learnerOpt.isPresent() && learnerOpt.get().getRoles().contains(com.artflowstudio.enums.Role.LEARNER)) {
            User learner = learnerOpt.get();
            model.addAttribute("learner", learner);
            model.addAttribute("pageTitle", "Chi tiết Học viên: " + learner.getFullName());

            List<Enrollment> enrollments = enrollmentService.findEnrollmentsWithDetailsByUserId(id);
            System.out.println("ADMIN CONTROLLER - viewLearner - Number of enrollments found: " + (enrollments != null ? enrollments.size() : "NULL")); // LOG 2
            if (enrollments != null && !enrollments.isEmpty()) {
                System.out.println("ADMIN CONTROLLER - viewLearner - First enrollment course: " + enrollments.get(0).getClassSchedule().getCourse().getName()); // LOG 3
            }

            model.addAttribute("enrollments", enrollments);

            return "admin/learners/view";
        } else {
            System.out.println("ADMIN CONTROLLER - viewLearner - Learner not found or not a learner for ID: " + id); // LOG 4
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy học viên hoặc người dùng không phải là học viên.");
            return "redirect:/admin/learners";
        }
    }

    @PostMapping("/learners/toggle-status/{id}")
    public String toggleLearnerStatus(@PathVariable("id") Long id, @RequestParam("enable") boolean enable, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserStatus(id, enable);
            redirectAttributes.addFlashAttribute("successMessage", "Trạng thái học viên đã được cập nhật.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật trạng thái học viên: " + e.getMessage());
        }
        return "redirect:/admin/learners";
    }
    @GetMapping("/grades/manage")
    public String listEnrollmentsForGrading(Model model) {
        List<Enrollment> enrollments = enrollmentService.findAllEnrollmentsForGrading();
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("pageTitle", "Quản lý Điểm/Đánh giá Học viên");
        return "admin/grades/list-enrollments";
    }

    @GetMapping("/grades/form/{enrollmentId}")
    public String gradeForm(@PathVariable("enrollmentId") Long enrollmentId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Enrollment> enrollmentOpt = enrollmentService.findEnrollmentByIdWithDetails(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đăng ký học.");
            return "redirect:/admin/grades/manage";
        }

        Enrollment enrollment = enrollmentOpt.get();
        GradeDto gradeDto = new GradeDto();
        gradeDto.setEnrollmentId(enrollmentId);

        Optional<Grade> existingGradeOpt = gradeService.getGradeByEnrollmentId(enrollmentId);
        if (existingGradeOpt.isPresent()) {
            Grade existingGrade = existingGradeOpt.get();
            gradeDto.setScore(existingGrade.getScore());
            gradeDto.setFeedback(existingGrade.getFeedback());
        }

        model.addAttribute("gradeDto", gradeDto);
        model.addAttribute("enrollment", enrollment); // Để hiển thị thông tin học viên/lớp
        model.addAttribute("pageTitle", "Nhập Điểm/Đánh giá cho: " + enrollment.getUser().getFullName() + " - Lớp: " + enrollment.getClassSchedule().getCourse().getName());
        return "admin/grades/form";
    }

    @PostMapping("/grades/save")
    public String saveGrade(@Valid @ModelAttribute("gradeDto") GradeDto gradeDto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            Optional<Enrollment> enrollmentOpt = enrollmentService.findEnrollmentByIdWithDetails(gradeDto.getEnrollmentId());
            if (enrollmentOpt.isPresent()) {
                model.addAttribute("enrollment", enrollmentOpt.get());
                model.addAttribute("pageTitle", "Nhập Điểm/Đánh giá cho: " + enrollmentOpt.get().getUser().getFullName() + " - Lớp: " + enrollmentOpt.get().getClassSchedule().getCourse().getName() + " - Lỗi");
            } else { // Should not happen if form is correct
                model.addAttribute("pageTitle", "Nhập Điểm/Đánh giá - Lỗi");
            }
            return "admin/grades/form";
        }

        try {
            gradeService.saveOrUpdateGrade(gradeDto);
            redirectAttributes.addFlashAttribute("successMessage", "Điểm/Đánh giá đã được lưu thành công!");
            return "redirect:/admin/grades/manage";
        } catch (Exception e) {
            Optional<Enrollment> enrollmentOpt = enrollmentService.findEnrollmentByIdWithDetails(gradeDto.getEnrollmentId());
            if (enrollmentOpt.isPresent()) {
                model.addAttribute("enrollment", enrollmentOpt.get());
                model.addAttribute("pageTitle", "Nhập Điểm/Đánh giá cho: " + enrollmentOpt.get().getUser().getFullName() + " - Lớp: " + enrollmentOpt.get().getClassSchedule().getCourse().getName() + " - Lỗi");
            }
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra khi lưu điểm/đánh giá: " + e.getMessage());
            return "admin/grades/form";
        }
    }
}