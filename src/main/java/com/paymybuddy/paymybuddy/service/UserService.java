package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        if (rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already used");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setBalanceCents(0L);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + normalizedEmail));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String email, String rawPassword) {
        User user = getByEmail(email);
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}