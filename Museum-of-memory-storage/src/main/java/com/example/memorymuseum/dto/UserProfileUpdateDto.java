package com.example.memorymuseum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    @Size(max = 100, message = "{user.fullname.size}")
    private String fullName;

    @NotEmpty(message = "{user.email.notempty}")
    @Email(message = "{user.email.invalid}")
    private String email;
    
    private String preferredLanguage;
}