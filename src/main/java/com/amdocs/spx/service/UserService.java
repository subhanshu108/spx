package com.amdocs.spx.service;


import com.amdocs.spx.entity.User;
import com.amdocs.spx.repository.TicketTypeRepository;
import com.amdocs.spx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user){
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    public List<User> getAllUsers(String name){
        return userRepository.findAll();
    }
}
