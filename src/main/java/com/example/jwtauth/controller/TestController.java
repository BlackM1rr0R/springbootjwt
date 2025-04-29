package com.example.jwtauth.controller;

import com.example.jwtauth.repository.UserRepository;
import com.example.jwtauth.service.UserService;
import com.example.jwtauth.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    private UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/hello")
    public String hello() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            UserDetails userDetails=(UserDetails) auth.getPrincipal();
            return "Hello " + username + " with roles: " + userDetails.getAuthorities();
        }
        return "Please log in";
    }
}
