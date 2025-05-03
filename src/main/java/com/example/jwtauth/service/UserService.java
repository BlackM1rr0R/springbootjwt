package com.example.jwtauth.service;

import com.example.jwtauth.repository.UserRepository;
import com.example.jwtauth.statusenum.VisaStatus;
import com.example.jwtauth.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User updateStatus(User incomingUser) {
        User user = userRepository.findByEmail(incomingUser.getEmail());
        if (user != null) {
            try {
                user.setEmail(incomingUser.getEmail());
                user.setUsername(incomingUser.getUsername());
                user.setRole(incomingUser.getRole());
                return userRepository.save(user);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Geçersiz status değeri: ");
            }
        }
        return null;
    }


    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User info(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> updateMissingPrices() {
        List<User> users = userRepository.findAll();
        List<User> updatedUsers = new ArrayList<>();
        for(User user : users) {

                updatedUsers.add(userRepository.save(user));

        }
        return updatedUsers;
    }


    public User changeUsername(User incomingUser) {
        User existingUser = userRepository.findByEmail(incomingUser.getEmail());
        if (existingUser != null) {
            existingUser.setUsername(incomingUser.getUsername());
            existingUser.setEmail(incomingUser.getEmail());
            existingUser.setPassword(passwordEncoder.encode(incomingUser.getPassword()));
            return userRepository.save(existingUser);
        }
        return null;
    }
}
