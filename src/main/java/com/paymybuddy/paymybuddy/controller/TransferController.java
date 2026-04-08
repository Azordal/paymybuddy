package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransferForm;
import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TransferController {

    // Service pour gérer les transferts d'argent
    private final TransactionService transactionService;

    // Service pour récupérer la liste des amis (connections)
    private final ConnectionService connectionService;

    // Injection des services dans le contrôleur
    public TransferController(TransactionService transactionService,
                              ConnectionService connectionService) {
        this.transactionService = transactionService;
        this.connectionService = connectionService;
    }

    /**
     * Affiche la page de transfert.
     * On récupère :
     * - l'utilisateur connecté
     * - sa liste d'amis
     * - son historique de transactions
     */
    @GetMapping("/transfer")
    public String showTransferPage(Model model,
                                   Authentication authentication) {

        // Email de l'utilisateur connecté
        String userEmail = authentication.getName();

        // Ajoute un formulaire vide si nécessaire
        if (!model.containsAttribute("transferForm")) {
            model.addAttribute("transferForm", new TransferForm());
        }

        // Récupère les amis de l'utilisateur
        List<Connection> friends =
                connectionService.listFriends(userEmail);

        model.addAttribute("friends", friends);

        // Récupère l'historique des transactions
        List<Transaction> transactions =
                transactionService.historyForUser(userEmail);

        model.addAttribute("transactions", transactions);

        return "transfer";
    }

    /**
     * Traite l'envoi d'un transfert.
     */
    @PostMapping("/transfer")
    public String sendMoney(
            @ModelAttribute("transferForm") TransferForm form,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String senderEmail = authentication.getName();

        try {
            // Conversion euros -> centimes
            long amountCents =
                    Math.round(form.getAmount() * 100);

            // Appel du service pour effectuer le transfert
            transactionService.sendMoney(
                    senderEmail,
                    form.getReceiverEmail(),
                    amountCents,
                    form.getDescription()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfert effectué avec succès."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            // Conserve les données du formulaire
            redirectAttributes.addFlashAttribute(
                    "transferForm",
                    form
            );
        }

        return "redirect:/transfer";
    }
}