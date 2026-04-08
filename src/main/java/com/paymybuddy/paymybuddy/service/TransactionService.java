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

    // Calculer les frais de 0,5 %
    private static final long FEE_NUMERATOR = 5;
    private static final long FEE_DENOMINATOR = 1000;

    // Repository pour accéder aux utilisateurs
    private final UserRepository userRepository;

    // Repository pour vérifier les connexions entre utilisateurs
    private final ConnectionRepository connectionRepository;

    // Repository pour enregistrer et lire les transactions
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository,
                              ConnectionRepository connectionRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Effectue un transfert d'argent entre deux utilisateurs.
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

        // Vérifie que les deux utilisateurs sont bien connectés
        if (!connectionRepository.existsByUser_IdAndFriend_Id(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Receiver is not in sender's connections");
        }

        long feeCents = computeFee(amountCents);
        long totalDebit = amountCents + feeCents;

        long senderBalance = safeCents(sender.getBalanceCents());
        if (senderBalance < totalDebit) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Mise à jour du solde de l'expéditeur
        sender.setBalanceCents(senderBalance - totalDebit);

        // Mise à jour du solde du destinataire
        long receiverBalance = safeCents(receiver.getBalanceCents());
        receiver.setBalanceCents(receiverBalance + amountCents);

        // Sauvegarde des nouveaux soldes
        userRepository.save(sender);
        userRepository.save(receiver);

        // Création et sauvegarde de la transaction
        Transaction tx = Transaction.internal(sender, receiver, amountCents, feeCents, description);
        return transactionRepository.save(tx);
    }

    /**
     * Retourne tout l'historique des transactions d'un utilisateur.
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
     * Retourne l'historique des transactions envoyées.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> historySent(String userEmail, Pageable pageable) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return transactionRepository.findBySender_IdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    /**
     * Retourne l'historique des transactions reçues.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> historyReceived(String userEmail, Pageable pageable) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return transactionRepository.findByReceiver_IdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    // Normalise et vérifie l'email
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

    // Calcule les frais à 0,5 %
    private long computeFee(long amountCents) {
        return (amountCents * FEE_NUMERATOR) / FEE_DENOMINATOR;
    }

    // Retourne 0 si la valeur est nulle
    private long safeCents(Long value) {
        return value == null ? 0L : value;
    }
}