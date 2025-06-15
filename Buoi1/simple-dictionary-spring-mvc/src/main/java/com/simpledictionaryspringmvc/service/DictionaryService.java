package com.simpledictionaryspringmvc.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DictionaryService {
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        // thêm cho tôi dữ liệu để tôi làm từ điển
        DICTIONARY.put("hello", "xin chào");
        DICTIONARY.put("world", "thế giới");
        DICTIONARY.put("java", "một ngôn ngữ lập trình");
        DICTIONARY.put("spring", "một framework cho Java");
        DICTIONARY.put("dictionary", "từ điển");
        DICTIONARY.put("service", "dịch vụ");
        DICTIONARY.put("controller", "bộ điều khiển");
        DICTIONARY.put("mvc", "mô hình - view - bộ điều khiển");
    }

    public String translate(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }
        return DICTIONARY.get(word.trim().toLowerCase());
    }
}


