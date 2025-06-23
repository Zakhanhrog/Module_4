package com.artflowstudio.controller;

import com.artflowstudio.dto.ClassScheduleDto;
import com.artflowstudio.entity.ClassSchedule;
import com.artflowstudio.entity.Course;
import com.artflowstudio.exception.ResourceNotFoundException;
import com.artflowstudio.service.ClassScheduleService;
import com.artflowstudio.service.CourseService;
import com.artflowstudio.service.InstructorService;
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

    @Autowired
    public AdminController(CourseService courseService,
                           ClassScheduleService classScheduleService,
                           InstructorService instructorService) {
        this.courseService = courseService;
        this.classScheduleService = classScheduleService;
        this.instructorService = instructorService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        // Có thể thêm các thống kê, thông tin nhanh ra đây
        return "admin/dashboard"; // Sẽ tạo view này
    }

    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.findAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "Quản lý Khóa học");
        return "admin/courses/list"; // Sẽ tạo view này
    }

    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("pageTitle", "Thêm Khóa học Mới");
        return "admin/courses/form"; // Sẽ tạo view này
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
}