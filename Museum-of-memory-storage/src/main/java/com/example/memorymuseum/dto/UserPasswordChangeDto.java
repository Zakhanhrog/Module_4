package com.example.memorymuseum.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordChangeDto {
    @NotEmpty(message = "{user.currentpassword.notempty}")
    private String currentPassword;

    @NotEmpty(message = "{user.newpassword.notempty}")
    @Size(min = 6, message = "{user.password.size}")
    private String newPassword;

    @NotEmpty(message = "{user.confirmnewpassword.notempty}")
    private String confirmNewPassword;
}