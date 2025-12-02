package com.ims.stockmanagement.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    // Note: Role is NOT accepted from user input for security reasons
    // All new users are automatically assigned USER role
}
