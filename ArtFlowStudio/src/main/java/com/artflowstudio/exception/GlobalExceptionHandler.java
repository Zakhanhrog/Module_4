package com.artflowstudio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException; // For 404

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCourseNotFoundException(CourseNotFoundException ex, Model model) {
        model.addAttribute("pageTitle", "Không tìm thấy khóa học");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        model.addAttribute("pageTitle", "Không tìm thấy tài nguyên");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoHandlerFoundException(NoHandlerFoundException ex, Model model) {
        model.addAttribute("pageTitle", "Trang không tồn tại");
        model.addAttribute("errorMessage", "Rất tiếc, trang bạn tìm kiếm không tồn tại hoặc đã bị di chuyển.");
        return "error/404";
    }


    @ExceptionHandler(ClassFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleClassFullException(ClassFullException ex, Model model) {
        model.addAttribute("pageTitle", "Lớp đã đầy");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("pageTitle", "Lỗi Hệ thống");
        model.addAttribute("errorMessage", "Đã có lỗi không mong muốn xảy ra. Vui lòng thử lại sau.");
        return "error/500";
    }
}