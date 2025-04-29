package com.example.jwtauth.controller;

import com.example.jwtauth.service.UserService;
import com.example.jwtauth.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@CrossOrigin(origins = "http://10.0.2.2")
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    public Map<String, Object> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Halihazırda giriş yapan kullanıcının adı
        User user = userService.info(email); // Bu metodu aşağıda da ekleyeceğiz
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("price", user.getPrice());
            if (user.getStatus() != null) {
                response.put("status", user.getStatus());
            }
            return response;
        }
        return Collections.emptyMap();
    }
    @PutMapping("/change-credentials")
    public ResponseEntity<?> changeCredentials(@RequestBody User user) {
        User updatedUser = userService.changeUsername(user);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kullanıcı bulunamadı");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<User> findAllUsers() {
        return userService.findAllUsers();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-status")
    public User updateStatus(@RequestBody User user) {
        return userService.updateStatus(user);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/info/{email}")
    public Map<String, Object> info(@PathVariable String email) {
        User user = userService.findByEmail(email); // yeni metod olmalı
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("price", user.getPrice());
            if (user.getStatus() == null) {
                response.put("status", user.getStatus());
            }
            return response;
        }
        return Collections.emptyMap();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-missing-prices")
    public List<User> updateMissingPrices() {
        return userService.updateMissingPrices();
    }

}
