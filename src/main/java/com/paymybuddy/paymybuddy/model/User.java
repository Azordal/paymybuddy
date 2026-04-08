package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    // Identifiant unique de l'utilisateur
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email de l'utilisateur (doit être unique)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Mot de passe stocké sous forme hashée (sécurisée)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Solde du portefeuille de l'utilisateur en centimes
    @Column(name = "balance_cents", nullable = false)
    private Long balanceCents = 0L;

    // Date de création du compte
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Date de dernière modification
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Définit automatiquement les dates lors de la création
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Met à jour la date lors d'une modification
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Long getBalanceCents() { return balanceCents; }
    public void setBalanceCents(Long balanceCents) { this.balanceCents = balanceCents; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
