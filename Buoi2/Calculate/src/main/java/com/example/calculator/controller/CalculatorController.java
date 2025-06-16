package com.example.calculator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalculatorController {

    @GetMapping("/form")
    public String showCalculatorForm(Model model) {
        if (!model.containsAttribute("number1")) {
            model.addAttribute("number1", "");
        }
        if (!model.containsAttribute("number2")) {
            model.addAttribute("number2", "");
        }
        return "calculatorForm";
    }

    @PostMapping("/calculate")
    public String calculate(
            @RequestParam(name = "number1", required = false) Double num1,
            @RequestParam(name = "number2", required = false) Double num2,
            @RequestParam(name = "operation") String operation,
            Model model) {

        model.addAttribute("number1", num1 != null ? num1 : "");
        model.addAttribute("number2", num2 != null ? num2 : "");

        if (num1 == null || num2 == null) {
            model.addAttribute("error", "Please enter valid numbers for both fields.");
            return "calculatorForm";
        }

        double result = 0;
        String operationName = "";
        boolean calculationError = false;
        String errorMessage = "";

        switch (operation.toLowerCase()) {
            case "addition":
                result = num1 + num2;
                operationName = "Addition";
                break;
            case "subtraction":
                result = num1 - num2;
                operationName = "Subtraction";
                break;
            case "multiplication":
                result = num1 * num2;
                operationName = "Multiplication";
                break;
            case "division":
                if (num2 == 0) {
                    calculationError = true;
                    errorMessage = "Cannot divide by zero.";
                } else {
                    result = num1 / num2;
                }
                operationName = "Division";
                break;
            default:
                calculationError = true;
                errorMessage = "Invalid operation selected.";
        }

        if (calculationError) {
            model.addAttribute("error", errorMessage);
        } else {
            model.addAttribute("result", result);
            model.addAttribute("operationName", operationName);
        }

        return "calculatorForm";
    }
}