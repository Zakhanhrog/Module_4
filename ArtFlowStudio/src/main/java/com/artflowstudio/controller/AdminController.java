package com.artflowstudio.controller;

import com.artflowstudio.entity.Course;
import com.artflowstudio.service.CourseService;
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
@RequestMapping("/admin") // Tất cả các mapping trong controller này sẽ bắt đầu bằng /admin
public class AdminController {

    private final CourseService courseService;
    // Các service khác cho admin sẽ thêm sau

    @Autowired
    public AdminController(CourseService courseService) {
        this.courseService = courseService;
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
}