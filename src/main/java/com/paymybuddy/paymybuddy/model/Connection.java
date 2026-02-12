package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "connections",
        uniqueConstraints = @UniqueConstraint(name = "uq_conn_pair", columnNames = {"user_id", "friend_id"}))
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructors
    public Connection() {}

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