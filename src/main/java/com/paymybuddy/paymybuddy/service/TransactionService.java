package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.ConnectionRepository;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    // 0.5% = 5 / 1000
    private static final long FEE_NUMERATOR = 5;
    private static final long FEE_DENOMINATOR = 1000;

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository,
                              ConnectionRepository connectionRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Transfert interne entre deux utilisateurs (amis).
     *
     * Règles :
     * - emails non nuls / non vides
     * - amountCents > 0
     * - sender != receiver
     * - receiver doit être dans les connections du sender
     * - solde sender >= amount + fee
     * - fee = 0.5% (arrondi inférieur)
     *
     * Effets :
     * - update solde sender et receiver
     * - insert en table transactions
     * - rollback complet si une erreur survient
     */
    @Transactional
    public Transaction sendMoney(String senderEmail,
                                 String receiverEmail,
                                 long amountCents,
                                 String description) {

        String s = normalizeEmail(senderEmail, "Sender email must not be null");
        String r = normalizeEmail(receiverEmail, "Receiver email must not be null");

        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents must be > 0");
        }
        if (s.equals(r)) {
            throw new IllegalArgumentException("Sender and receiver must be different");
        }

        User sender = userRepository.findByEmail(s)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + s));
        User receiver = userRepository.findByEmail(r)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + r));

        // Vérifie qu'ils sont connectés (amis)
        if (!connectionRepository.existsByUser_IdAndFriend_Id(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Receiver is not in sender's connections");
        }

        long feeCents = computeFee(amountCents);
        long totalDebit = amountCents + feeCents;

        long senderBalance = safeCents(sender.getBalanceCents());
        if (senderBalance < totalDebit) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Mise à jour soldes
        sender.setBalanceCents(senderBalance - totalDebit);

        long receiverBalance = safeCents(receiver.getBalanceCents());
        receiver.setBalanceCents(receiverBalance + amountCents);

        // Persist soldes
        userRepository.save(sender);
        userRepository.save(receiver);

        // Persist transaction
        Transaction tx = Transaction.internal(sender, receiver, amountCents, feeCents, description);
        return transactionRepository.save(tx);
    }

    /**
     * Historique complet (envoyé OU reçu) en List (simple).
     * Utile pour prototype.
     */
    @Transactional(readOnly = true)
    public List<Transaction> historyForUser(String userEmail) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return transactionRepository
                .findBySender_IdOrReceiver_IdOrderByCreatedAtDesc(user.getId(), user.getId());
    }

    /**
     * Historique envoyé.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> historySent(String userEmail, Pageable pageable) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return transactionRepository.findBySender_IdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * Historique reçu.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> historyReceived(String userEmail, Pageable pageable) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return transactionRepository.findByReceiver_IdOrderByCreatedAtDesc(user.getId(), pageable);
    }


    private String normalizeEmail(String email, String nullMessage) {
        if (email == null) {
            throw new IllegalArgumentException(nullMessage);
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        return normalized;
    }

    private long computeFee(long amountCents) {
        // 0.5% = 5/1000, arrondi inférieur en centimes
        return (amountCents * FEE_NUMERATOR) / FEE_DENOMINATOR;
    }

    private long safeCents(Long value) {
        return value == null ? 0L : value;
    }
}
