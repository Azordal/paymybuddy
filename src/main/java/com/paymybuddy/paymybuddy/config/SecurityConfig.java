package com.paymybuddy.paymybuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Service utilisé par Spring Security pour charger un utilisateur
     * à partir de son email dans la base de données.
     */
    private final CustomUserDetailsConfig customUserDetailsConfig;

    public SecurityConfig(CustomUserDetailsConfig customUserDetailsConfig) {
        this.customUserDetailsConfig = customUserDetailsConfig;
    }

    /**
     * Déclare le bean PasswordEncoder utilisé dans toute l'application.
     * BCrypt permet de hasher les mots de passe à l'inscription
     * et de vérifier ces mots de passe au moment de la connexion.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure le provider d'authentification.
     * Ce provider explique à Spring Security :
     * - comment charger un utilisateur (via CustomUserDetailsConfig)
     * - comment comparer les mots de passe (via BCrypt)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsConfig);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Configure les règles de sécurité HTTP de l'application.
     * Cette méthode définit :
     * - quelles pages sont publiques
     * - quelles pages nécessitent une connexion
     * - la page de login personnalisée
     * - l'URL de traitement du login
     * - la redirection après succès ou échec
     * - le comportement de logout
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authenticationProvider(authenticationProvider())

                // Déclare les routes autorisées sans authentification
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Configuration du formulaire de connexion
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/transfer", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // Configuration de la déconnexion
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}