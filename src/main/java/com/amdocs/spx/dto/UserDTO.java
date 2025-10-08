package com.amdocs.spx.dto;

import com.amdocs.spx.entity.User.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String passwordHash; // For registration/login
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role = Role.CUSTOMER;
    private Boolean isActive = true;
}