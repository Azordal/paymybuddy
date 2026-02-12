package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    boolean existsByUser_IdAndFriend_Id(Long userId, Long friendId);

    List<Connection> findByUser_Id(Long userId);
}