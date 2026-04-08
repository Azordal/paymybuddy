package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "connections",
        // Empêche de créer deux fois la même connexion entre deux utilisateurs
        uniqueConstraints = @UniqueConstraint(
                name = "uq_conn_pair",
                columnNames = {"user_id", "friend_id"}
        )
)
public class Connection {

    // Identifiant unique de la connexion
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisateur principal
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ami de l'utilisateur
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    // Date de création de la connexion
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Définit automatiquement la date lors de la création
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructeur vide requis par JPA
    public Connection() {}

    // Constructeur pour créer une connexion entre deux utilisateurs
    public Connection(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }

    // Getters / Setters
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getFriend() { return friend; }
    public void setFriend(User friend) { this.friend = friend; }

    public Instant getCreatedAt() { return createdAt; }
}