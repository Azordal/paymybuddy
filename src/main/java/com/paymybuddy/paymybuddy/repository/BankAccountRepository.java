package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    boolean existsByUser_IdAndIban(Long userId, String iban);

    List<BankAccount> findByUser_Id(Long userId);
}
