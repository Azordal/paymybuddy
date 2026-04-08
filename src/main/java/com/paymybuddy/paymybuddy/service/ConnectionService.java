package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.ConnectionRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConnectionService {

    // Repository pour accéder aux utilisateurs
    private final UserRepository userRepository;

    // Repository pour accéder aux connexions
    private final ConnectionRepository connectionRepository;

    // Injection des repositories
    public ConnectionService(UserRepository userRepository,
                             ConnectionRepository connectionRepository) {
        this.userRepository = userRepository;
        this.connectionRepository = connectionRepository;
    }

    /**
     * Ajoute un ami à partir de son email.
     */
    @Transactional
    public Connection addFriendByEmail(String userEmail, String friendEmail) {
        if (userEmail == null || friendEmail == null) {
            throw new IllegalArgumentException("Emails must not be null");
        }

        String u = userEmail.trim().toLowerCase();
        String f = friendEmail.trim().toLowerCase();

        if (u.isEmpty() || f.isEmpty()) {
            throw new IllegalArgumentException("Emails must not be empty");
        }

        if (u.equals(f)) {
            throw new IllegalArgumentException("You cannot add yourself as a friend");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        User friend = userRepository.findByEmail(f)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found: " + f));

        // Vérifie si la connexion existe déjà
        if (connectionRepository.existsByUser_IdAndFriend_Id(user.getId(), friend.getId())) {
            return connectionRepository.findByUser_Id(user.getId()).stream()
                    .filter(c -> c.getFriend().getId().equals(friend.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Connection exists but cannot be loaded"));
        }

        Connection connection = new Connection(user, friend);
        return connectionRepository.save(connection);
    }

    /**
     * Liste les amis d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<Connection> listFriends(String userEmail) {
        if (userEmail == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        String u = userEmail.trim().toLowerCase();
        if (u.isEmpty()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        return connectionRepository.findByUser_Id(user.getId());
    }

    /**
     * Vérifie si deux utilisateurs sont amis.
     */
    @Transactional(readOnly = true)
    public boolean isFriend(String userEmail, String friendEmail) {
        if (userEmail == null || friendEmail == null) {
            throw new IllegalArgumentException("Emails must not be null");
        }

        String u = userEmail.trim().toLowerCase();
        String f = friendEmail.trim().toLowerCase();

        if (u.isEmpty() || f.isEmpty()) {
            throw new IllegalArgumentException("Emails must not be empty");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        User friend = userRepository.findByEmail(f)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found: " + f));

        return connectionRepository.existsByUser_IdAndFriend_Id(user.getId(), friend.getId());
    }

    /**
     * Supprime un ami.
     */
    @Transactional
    public void removeFriendByEmail(String userEmail, String friendEmail) {
        if (userEmail == null || friendEmail == null) {
            throw new IllegalArgumentException("Emails must not be null");
        }

        String u = userEmail.trim().toLowerCase();
        String f = friendEmail.trim().toLowerCase();

        if (u.isEmpty() || f.isEmpty()) {
            throw new IllegalArgumentException("Emails must not be empty");
        }

        User user = userRepository.findByEmail(u)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + u));

        User friend = userRepository.findByEmail(f)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found: " + f));

        connectionRepository.findByUser_Id(user.getId()).stream()
                .filter(c -> c.getFriend().getId().equals(friend.getId()))
                .findFirst()
                .ifPresent(connectionRepository::delete);
    }
}