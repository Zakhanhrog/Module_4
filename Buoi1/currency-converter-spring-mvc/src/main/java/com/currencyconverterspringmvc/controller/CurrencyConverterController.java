package com.currencyconverterspringmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CurrencyConverterController {

    @GetMapping("/converter")
    public String showConverterForm() {
        return "converter";
    }

    @PostMapping("/converter/submit")
    public String convertCurrency(
            @RequestParam(name = "exchangeRate", required = false) Double exchangeRate,
            @RequestParam(name = "usdAmount", required = false) Double usdAmount,
            Model model) {

        if (exchangeRate == null || usdAmount == null) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ tỷ giá và số tiền USD.");
            if (exchangeRate != null) model.addAttribute("exchangeRate", exchangeRate);
            if (usdAmount != null) model.addAttribute("usdAmount", usdAmount);
            return "converter";
        }

        if (exchangeRate <= 0 || usdAmount < 0) {
            model.addAttribute("errorMessage", "Tỷ giá phải lớn hơn 0 và số tiền USD không được âm.");
            model.addAttribute("exchangeRate", exchangeRate);
            model.addAttribute("usdAmount", usdAmount);
            return "converter";
        }

        double vndAmount = exchangeRate * usdAmount;

        model.addAttribute("exchangeRate", exchangeRate);
        model.addAttribute("usdAmount", usdAmount);
        model.addAttribute("vndAmount", vndAmount);
        model.addAttribute("successMessage", "Chuyển đổi thành công!");

        return "converter";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/converter";
    }
}