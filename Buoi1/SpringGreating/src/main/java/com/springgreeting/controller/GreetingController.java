package com.springgreeting.controller;
import com.springgreeting.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class GreetingController {

   /* @Autowired
    private GreetingService greetingService;
    public void setGreetingController(GreetingService greetingService) {
            this.greetingService = greetingService;
    }*/

    /*@Autowired
    private GreetingService greetingService;*/

    @Autowired
    private GreetingService greetingService;
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greeting")
    public String greeting() {
        String greeting = greetingService.getGreeting();
        return "index";
    }
}