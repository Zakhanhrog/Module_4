package com.simpledictionaryspringmvc.controller;

import com.simpledictionaryspringmvc.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DictionaryController {
    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/dictionary")
    public String showDictionaryPage() {
        return "dictionary";
    }

    @PostMapping("/dictionary/lookup")
    public String lookupWord(
            @RequestParam("word") String word,
            Model model) {
        String vietnameseMeaning = null;
        String errorMessage = null;

        if (word == null || word.trim().isEmpty()) {
            errorMessage = "Vui lòng nhập một từ để tra cứu.";
        } else {
            vietnameseMeaning = dictionaryService.translate(word);
            if (vietnameseMeaning == null) {
                errorMessage = "Không tìm thấy nghĩa của từ: " + word;
            }
        }

        model.addAttribute("searchedWord", word);
        if (vietnameseMeaning != null) {
            model.addAttribute("vietnameseMeaning", vietnameseMeaning);
        } else {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "dictionary";
    }

    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/dictionary";
    }

}
