package com.amdocs.spx.service;

import com.amdocs.spx.dto.UserDTO;
import com.amdocs.spx.dto.UserDTO2;
import com.amdocs.spx.entity.User;
import com.amdocs.spx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public List<User> getAllOrganizers(){
        List<User> users =  userRepository.findAll();
        List<User> toReturn = new ArrayList<>();
        for(User user : users){
            if(user.getRole() == User.Role.ORGANIZER){
                toReturn.add(user);
            }
        }
        return toReturn;
    }

    public User loginUser(String usernameOrEmail, String password) {
        // Find user by username or email
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String deleteUser(Long id) {
        userRepository.deleteById(id);
        return "User Deleted with id " + id;
    }

    public User editUser(Long id, UserDTO2 userDTO2) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setUsername(userDTO2.getUsername());
        user.setEmail(userDTO2.getEmail());

        user.setIsActive(userDTO2.getIsActive());
        user.setRole(userDTO2.getRole());
        user.setFirstName(userDTO2.getFirstName());
        user.setLastName(userDTO2.getLastName());
        user.setPhoneNumber(userDTO2.getPhoneNumber());
        return userRepository.save(user);


    }
}