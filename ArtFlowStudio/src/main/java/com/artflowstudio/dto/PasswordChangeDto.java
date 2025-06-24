package com.artflowstudio.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeDto {

    @NotEmpty(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotEmpty(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword;

    @NotEmpty(message = "Xác nhận mật khẩu mới không được để trống")
    private String confirmNewPassword;
}