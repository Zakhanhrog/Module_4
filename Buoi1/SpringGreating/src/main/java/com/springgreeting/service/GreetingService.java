package com.springgreeting.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String getGreeting() {
        return "Hello, Codegym Gia Khanh!";
    }
}
