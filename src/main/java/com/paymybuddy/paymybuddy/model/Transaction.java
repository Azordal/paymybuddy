package com.paymybuddy.paymybuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INTERNAL / TOPUP / WITHDRAW
    @Column(nullable = false, length = 20)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "fee_cents", nullable = false)
    private Long feeCents = 0L;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.feeCents == null) this.feeCents = 0L;
    }

    // Constructors
    public Transaction() {}

    // Internal transfer
    public static Transaction internal(User sender, User receiver, long amountCents, long feeCents, String description) {
        Transaction tx = new Transaction();
        tx.type = "INTERNAL";
        tx.sender = sender;
        tx.receiver = receiver;
        tx.amountCents = amountCents;
        tx.feeCents = feeCents;
        tx.description = description;
        return tx;
    }

    // Top up from bank
    public static Transaction topup(User receiver, BankAccount bankAccount, long amountCents, String description) {
        Transaction tx = new Transaction();
        tx.type = "TOPUP";
        tx.receiver = receiver;
        tx.bankAccount = bankAccount;
        tx.amountCents = amountCents;
        tx.feeCents = 0L;
        tx.description = description;
        return tx;
    }

    // Withdraw to bank
    public static Transaction withdraw(User sender, BankAccount bankAccount, long amountCents, String description) {
        Transaction tx = new Transaction();
        tx.type = "WITHDRAW";
        tx.sender = sender;
        tx.bankAccount = bankAccount;
        tx.amountCents = amountCents;
        tx.feeCents = 0L;
        tx.description = description;
        return tx;
    }

    // Getters / Setters
    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public Long getAmountCents() { return amountCents; }
    public void setAmountCents(Long amountCents) { this.amountCents = amountCents; }

    public Long getFeeCents() { return feeCents; }
    public void setFeeCents(Long feeCents) { this.feeCents = feeCents; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
}
