package com.example.chat.service;

import com.example.chat.exception.CustomException;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                logger.warn("Registration failed: Username {} already exists", username);
                throw new CustomException("Username already exists", 400);
            }
            if (userRepository.findByEmail(email).isPresent()) {
                logger.warn("Registration failed: Email {} already exists", email);
                throw new CustomException("Email already exists", 400);
            }
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setCreatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}", username);
            return savedUser;
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage(), e);
            throw new CustomException("Failed to register user: " + e.getMessage(), 500);
        }
    }

    public User loginUser(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Login failed: Username {} not found", username);
                        return new CustomException("Invalid username or password", 401);
                    });
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Login failed: Invalid password for username {}", username);
                throw new CustomException("Invalid username or password", 401);
            }
            logger.info("User logged in successfully: {}", username);
            return user;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error logging in user: {}", e.getMessage(), e);
            throw new CustomException("Failed to login: " + e.getMessage(), 500);
        }
    }
}

