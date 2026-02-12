package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "bank_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_iban", columnNames = {"user_id", "iban"})
)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 34)
    private String iban;

    @Column(length = 120)
    private String label;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructors
    public BankAccount() {}

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
