package com.example.memorymuseum.controller;

import com.example.memorymuseum.dto.UserRegistrationDto;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public AuthController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", messageSource.getMessage("login.error", null, LocaleContextHolder.getLocale()));
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", messageSource.getMessage("logout.success", null, LocaleContextHolder.getLocale()));
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("userDto")) { // Avoid overwriting if redirected with errors
            model.addAttribute("userDto", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("userDto") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch",
                    messageSource.getMessage("password.mismatch", null, LocaleContextHolder.getLocale()));
        }
        if (userService.findByUsername(registrationDto.getUsername()).isPresent()) {
            result.rejectValue("username", "username.exists",
                    messageSource.getMessage("username.exists", null, LocaleContextHolder.getLocale()));
        }
        if (userService.findByEmail(registrationDto.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists",
                    messageSource.getMessage("email.exists", null, LocaleContextHolder.getLocale()));
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userDto", result);
            redirectAttributes.addFlashAttribute("userDto", registrationDto);
            return "redirect:/register";
        }

        userService.registerNewUserAccount(registrationDto);
        redirectAttributes.addFlashAttribute("registrationSuccess",
                "Đăng ký thành công! Vui lòng đăng nhập."); // Sẽ I18N sau
        return "redirect:/login";
    }
}