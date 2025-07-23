package com.example.chat.controller;

import com.example.chat.exception.CustomException;
import com.example.chat.model.User;
import com.example.chat.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
            logger.info("registration conpleted for : {} user",user);
            return ResponseEntity.ok(registeredUser);
        } catch (CustomException e) {
            logger.warn("Registration error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        try {
            User loggedInUser = authService.loginUser(user.getUsername(), user.getPassword());
            logger.info("Login Sucessful for user {} ",user);
            return ResponseEntity.ok(loggedInUser);
        } catch (CustomException e) {
            logger.warn("Login error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}

