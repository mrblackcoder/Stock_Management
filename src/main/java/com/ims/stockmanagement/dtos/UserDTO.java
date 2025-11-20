package com.ims.stockmanagement.dtos;

import com.ims.stockmanagement.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;
}

