package com.paymybuddy.paymybuddy.config;

import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsConfig implements org.springframework.security.core.userdetails.UserDetailsService {

    /**
     * Repository utilisé pour rechercher un utilisateur dans la table users à partir de son email.
     */
    private final UserRepository userRepository;

    public CustomUserDetailsConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Méthode appelée automatiquement par Spring Security
     * Retourne un objet UserDetails compatible avec Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String email = username.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable : " + email)
                );

        /**
         * On retourne ici un utilisateur au format attendu par Spring Security :
         * - l'email
         * - le mot de passe hashé
         * - le rôle de l'utilisateur
         */
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
