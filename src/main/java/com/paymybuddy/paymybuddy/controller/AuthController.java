package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.RegisterForm;
import com.paymybuddy.paymybuddy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    // Service utilisé pour gérer l'inscription des utilisateurs
    private final UserService userService;

    // Injection du service dans le contrôleur
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Affiche la page de connexion
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Affiche la page d'inscription
    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        // Ajoute un formulaire vide si aucun formulaire n'est déjà présent
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }

        return "register";
    }

    // Traite l'envoi du formulaire d'inscription
    @PostMapping("/register")
    public String register(@ModelAttribute("registerForm") RegisterForm form,
                           RedirectAttributes redirectAttributes) {
        try {
            // Appelle le service pour créer l'utilisateur
            userService.register(form.getEmail(), form.getPassword());

            // Message de succès après inscription
            redirectAttributes.addFlashAttribute("successMessage", "Compte créé avec succès.");

            // Redirection vers la page de connexion
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // Message d'erreur si l'inscription échoue
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // Conserve les données déjà saisies
            redirectAttributes.addFlashAttribute("registerForm", form);

            // Retour vers la page d'inscription
            return "redirect:/register";
        }
    }
}