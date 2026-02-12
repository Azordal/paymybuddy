package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Historique envoyé par un user
    Page<Transaction> findBySender_IdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    // Historique reçu par un user
    Page<Transaction> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // Historique complet (envoyé OU reçu)
    List<Transaction> findBySender_IdOrReceiver_IdOrderByCreatedAtDesc(Long senderId, Long receiverId);
}