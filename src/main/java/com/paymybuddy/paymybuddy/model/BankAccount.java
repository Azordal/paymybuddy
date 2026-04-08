package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "bank_accounts",
        // Empêche un utilisateur d'avoir deux fois le même IBAN
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_iban",
                columnNames = {"user_id", "iban"}
        )
)
public class BankAccount {

    // Identifiant unique du compte bancaire
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation : plusieurs comptes peuvent appartenir à un utilisateur
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // IBAN du compte bancaire
    @Column(nullable = false, length = 34)
    private String iban;

    // Nom du compte
    @Column(length = 120)
    private String label;

    // Date de création du compte
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Définit automatiquement la date lors de la création
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructeur vide requis par JPA
    public BankAccount() {}

    // Constructeur avec paramètres
    public BankAccount(User user, String iban, String label) {
        this.user = user;
        this.iban = iban;
        this.label = label;
    }

    // Getters / Setters
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getCreatedAt() { return createdAt; }
}
