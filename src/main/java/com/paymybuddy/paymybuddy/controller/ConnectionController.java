package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.AddRelationForm;
import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ConnectionController {

    // Service qui gère les relations entre utilisateurs
    private final ConnectionService connectionService;

    // Injection du service dans le contrôleur
    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Affiche la page d'ajout de relation.
     * On prépare :
     * - le formulaire
     * - la liste actuelle des amis
     */
    @GetMapping("/relations/add")
    public String showAddRelationPage(Model model, Authentication authentication) {

        // Email de l'utilisateur connecté
        String userEmail = authentication.getName();

        // Ajoute un formulaire vide si nécessaire
        if (!model.containsAttribute("relationDto")) {
            model.addAttribute("relationDto", new AddRelationForm());
        }

        // Récupère la liste des amis
        List<Connection> friends = connectionService.listFriends(userEmail);
        model.addAttribute("friends", friends);

        return "add-relation";
    }

    /**
     * Traite l'ajout d'une nouvelle relation.
     */
    @PostMapping("/relations/add")
    public String addRelation(@ModelAttribute("relationDto") AddRelationForm form,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        // Email de l'utilisateur connecté
        String userEmail = authentication.getName();

        try {
            // Appelle le service pour créer la relation
            connectionService.addFriendByEmail(userEmail, form.getEmail());

            // Message de succès
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Relation ajoutée avec succès."
            );

        } catch (IllegalArgumentException e) {

            // Message d'erreur
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            // Conserve les données du formulaire
            redirectAttributes.addFlashAttribute(
                    "relationDto",
                    form
            );
        }

        return "redirect:/relations/add";
    }
}