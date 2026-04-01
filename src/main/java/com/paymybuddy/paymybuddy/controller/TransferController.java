package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.dto.TransferForm;
import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TransferController {

    private final TransactionService transactionService;
    private final ConnectionService connectionService;

    public TransferController(TransactionService transactionService,
                              ConnectionService connectionService) {
        this.transactionService = transactionService;
        this.connectionService = connectionService;
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model, Authentication authentication) {
        String userEmail = authentication.getName();

        if (!model.containsAttribute("transferDto")) {
            model.addAttribute("transferDto", new TransferForm());
        }

        List<Connection> relations = connectionService.listFriends(userEmail);
        model.addAttribute("relations", relations);

        List<Transaction> transactions = transactionService.historyForUser(userEmail);
        model.addAttribute("transactions", transactions);

        return "transfer";
    }

    @PostMapping("/transfer")
    public String sendMoney(@ModelAttribute("transferDto") TransferForm form,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        String senderEmail = authentication.getName();

        try {
            transactionService.sendMoney(
                    senderEmail,
                    form.getReceiverEmail(),
                    form.getAmountCents(),
                    form.getDescription()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Transfert effectué avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("transferDto", form);
        }

        return "redirect:/transfer";
    }
}