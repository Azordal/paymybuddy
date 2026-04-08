package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    // Repository pour accéder aux utilisateurs
    private final UserRepository userRepository;

    // Composant pour encoder les mots de passe
    private final PasswordEncoder passwordEncoder;

    // Injection des dépendances
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Enregistre un nouvel utilisateur
    @Transactional
    public User register(String email, String rawPassword) {

        // Vérifie que les paramètres existent
        if (email == null || rawPassword == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }

        // Normalise l'email
        String normalizedEmail = email.trim().toLowerCase();

        // Vérifie que l'email n'est pas vide
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        // Vérifie que le mot de passe n'est pas vide
        if (rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        // Vérifie que l'email n'est pas déjà utilisé
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already used");
        }

        // Création du nouvel utilisateur
        User user = new User();
        user.setEmail(normalizedEmail);

        // Encodage sécurisé du mot de passe
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        // Solde initial à 0
        user.setBalanceCents(0L);

        // Sauvegarde en base
        return userRepository.save(user);
    }

    // Récupère un utilisateur par email
    @Transactional(readOnly = true)
    public User getByEmail(String email) {

        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + normalizedEmail));
    }

    // Récupère un utilisateur par id
    @Transactional(readOnly = true)
    public User getById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    // Vérifie si le mot de passe est correct
    @Transactional(readOnly = true)
    public boolean checkPassword(String email, String rawPassword) {

        User user = getByEmail(email);

        // Compare le mot de passe saisi avec le mot de passe stocké
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}