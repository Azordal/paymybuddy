package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BankAccountService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(UserRepository userRepository,
                              BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Ajoute un compte bancaire à un utilisateur.
     *
     * Règles:
     * - userEmail existe
     * - iban non vide
     * - iban normalisé (sans espaces, en majuscule)
     * - unicité (user_id, iban)
     * - label optionnel
     */
    @Transactional
    public BankAccount addBankAccount(String userEmail, String iban, String label) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        if (iban == null) {
            throw new IllegalArgumentException("IBAN must not be null");
        }
        String normalizedIban = normalizeIban(iban);
        if (normalizedIban.isEmpty()) {
            throw new IllegalArgumentException("IBAN must not be empty");
        }
        if (!looksLikeIban(normalizedIban)) {
            throw new IllegalArgumentException("IBAN format looks invalid");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        // Evite doublons (user_id, iban)
        if (bankAccountRepository.existsByUser_IdAndIban(user.getId(), normalizedIban)) {
            // Choix UX: renvoyer l'existant plutôt que planter.
            // (ou throw "Already exists")
            return bankAccountRepository.findByUser_Id(user.getId()).stream()
                    .filter(a -> a.getIban().equals(normalizedIban))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Bank account exists but cannot be loaded"));
        }

        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setIban(normalizedIban);
        account.setLabel(cleanLabel(label));

        return bankAccountRepository.save(account);
    }

    /**
     * Liste les comptes bancaires d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<BankAccount> listForUser(String userEmail) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return bankAccountRepository.findByUser_Id(user.getId());
    }

    /**
     * Récupère un compte par id en s'assurant qu'il appartient au user.
     * Utile pour TOPUP/WITHDRAW plus tard.
     */
    @Transactional(readOnly = true)
    public BankAccount getOwnedByUser(String userEmail, Long bankAccountId) {
        String u = normalizeEmail(userEmail, "Email must not be null");

        if (bankAccountId == null) {
            throw new IllegalArgumentException("bankAccountId must not be null");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        BankAccount account = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        if (account.getUser() == null || account.getUser().getId() == null) {
            throw new IllegalStateException("Bank account has no owner");
        }

        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bank account does not belong to this user");
        }

        return account;
    }

    /**
     * Supprime un compte bancaire (si appartient au user).
     */
    @Transactional
    public void deleteOwnedByUser(String userEmail, Long bankAccountId) {
        BankAccount owned = getOwnedByUser(userEmail, bankAccountId);
        bankAccountRepository.delete(owned);
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

    private String normalizeIban(String iban) {
        return iban.replaceAll("\\s+", "").toUpperCase();
    }

    private boolean looksLikeIban(String iban) {
        // Validation "light" (prototype) :
        // - longueur 15 à 34
        // - commence par 2 lettres + 2 chiffres
        // - reste alphanum
        int len = iban.length();
        if (len < 15 || len > 34) return false;

        if (!Character.isLetter(iban.charAt(0)) || !Character.isLetter(iban.charAt(1))) return false;
        if (!Character.isDigit(iban.charAt(2)) || !Character.isDigit(iban.charAt(3))) return false;

        for (int i = 0; i < len; i++) {
            char c = iban.charAt(i);
            if (!Character.isLetterOrDigit(c)) return false;
        }
        return true;
    }

    private String cleanLabel(String label) {
        if (label == null) return null;
        String trimmed = label.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
