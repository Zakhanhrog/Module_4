package com.example.loginapp.controller;

import com.example.loginapp.model.Login;
import com.example.loginapp.model.User;
import com.example.loginapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public ModelAndView showLoginForm() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("login", new Login());
        return modelAndView;
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute("login") Login login, Model model) {
        User user = userService.checkLogin(login);

        if (user != null) {
            model.addAttribute("user", user);
            return "user";
        } else {
            model.addAttribute("error", "Invalid account or password. Please try again.");
            return "login";
        }
    }
}