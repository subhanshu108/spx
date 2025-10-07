package com.amdocs.spx.controller;

import com.amdocs.spx.entity.User;
import com.amdocs.spx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Register new user account - matches UserService.registerUser()
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // 2. Retrieve user details by ID - matches UserService.getUserById()
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. Get all users - matches UserService.getAllUsers()
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String name) {
        List<User> users = userService.getAllUsers(name);
        return ResponseEntity.ok(users);
    }
}