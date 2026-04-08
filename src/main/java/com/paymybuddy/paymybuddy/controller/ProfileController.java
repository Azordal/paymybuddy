package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    // Service utilisé pour récupérer les informations de l'utilisateur
    private final UserService userService;

    // Injection du service dans le contrôleur
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Affiche la page de profil de l'utilisateur connecté
    @GetMapping("/profile")
    public String showProfilePage(Model model, Authentication authentication) {

        // Récupère l'email de l'utilisateur actuellement authentifié
        String userEmail = authentication.getName();

        // Recherche l'utilisateur correspondant dans la base de données
        User user = userService.getByEmail(userEmail);

        // Envoie l'objet utilisateur à la vue pour affichage
        model.addAttribute("user", user);

        // Retourne le nom de la page Thymeleaf à afficher
        return "profile";
    }
}