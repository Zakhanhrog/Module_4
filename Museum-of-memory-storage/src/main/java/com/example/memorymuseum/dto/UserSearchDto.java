package com.example.memorymuseum.dto;

import com.example.memorymuseum.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDto {
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private Boolean enabled;
    private Boolean deleted;
}
