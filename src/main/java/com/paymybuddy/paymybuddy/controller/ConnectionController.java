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

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping("/relations/add")
    public String showAddRelationPage(Model model, Authentication authentication) {
        String userEmail = authentication.getName();

        if (!model.containsAttribute("relationDto")) {
            model.addAttribute("relationDto", new AddRelationForm());
        }

        List<Connection> friends = connectionService.listFriends(userEmail);
        model.addAttribute("friends", friends);

        return "add-relation";
    }

    @PostMapping("/relations/add")
    public String addRelation(@ModelAttribute("relationDto") AddRelationForm form,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        String userEmail = authentication.getName();

        try {
            connectionService.addFriendByEmail(userEmail, form.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Relation ajoutée avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("relationDto", form);
        }

        return "redirect:/relations/add";
    }
}