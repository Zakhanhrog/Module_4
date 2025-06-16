package com.example.sandwich.controller;

import com.example.sandwich.model.Sandwich;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
public class SandwichController {

    private final String[] availableCondiments = {"Lettuce", "Tomato", "Mustard", "Sprouts"};

    @GetMapping("/condimentForm")
    public String showCondimentForm(Model model) {
        model.addAttribute("sandwich", new Sandwich());
        model.addAttribute("allCondiments", availableCondiments);
        return "condimentForm";
    }

    @PostMapping("/save")
    public String saveCondiments(@ModelAttribute("sandwich") Sandwich sandwich, Model model) {
        String[] selected = sandwich.getCondiments();
        if (selected == null || selected.length == 0) {
            model.addAttribute("message", "No condiments selected.");
        } else {
            model.addAttribute("message", "Selected condiments: " + String.join(", ", selected));
            model.addAttribute("selectedCondiments", Arrays.asList(selected));
        }
        return "result";
    }
}