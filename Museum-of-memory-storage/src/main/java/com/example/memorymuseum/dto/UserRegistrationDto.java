package com.example.memorymuseum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotEmpty(message = "{user.username.notempty}")
    @Size(min = 4, max = 50, message = "{user.username.size}")
    private String username;

    @NotEmpty(message = "{user.email.notempty}")
    @Email(message = "{user.email.invalid}")
    private String email;

    @NotEmpty(message = "{user.password.notempty}")
    @Size(min = 6, message = "{user.password.size}")
    private String password;

    @NotEmpty(message = "{user.confirmpassword.notempty}")
    private String confirmPassword;

    private String fullName;
}