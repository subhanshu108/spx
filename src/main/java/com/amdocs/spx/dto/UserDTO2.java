package com.amdocs.spx.dto;


import com.amdocs.spx.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO2 {
    private Long userId;
    private String username;
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private User.Role role = User.Role.CUSTOMER;
    private Boolean isActive = true;
}