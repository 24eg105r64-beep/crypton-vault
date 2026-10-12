package com.crypton.controller;

import com.crypton.model.Asset;
import com.crypton.model.User;
import com.crypton.service.AssetService;
import com.crypton.service.TransactionService;
import com.crypton.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final TransactionService transactionService;
    private final UserService userService;

    private User currentUser(UserDetails principal) {
        return userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Signed-in user not found"));
    }

    private BigDecimal parseAmount(String amount) {
        try {
            return new BigDecimal(amount.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Please enter a valid amount, like 0.5");
        }
    }

    @GetMapping("/{assetId}")
    public String viewAsset(@PathVariable Long assetId,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        User user = currentUser(principal);
        Asset asset = assetService.getAssetById(assetId, user);
        model.addAttribute("user", user);
        model.addAttribute("asset", asset);
        model.addAttribute("transactions", transactionService.getAssetTransactionHistory(assetId, user));
        return "asset-detail";
    }

    @PostMapping("/{assetId}/deposit")
    public String deposit(@PathVariable Long assetId,
                          @RequestParam String amount,
                          @AuthenticationPrincipal UserDetails principal,
                          RedirectAttributes ra) {
        try {
            transactionService.deposit(assetId, parseAmount(amount), currentUser(principal));
            ra.addFlashAttribute("success", "Deposit complete. Your balance has been updated.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/" + assetId;
    }

    @PostMapping("/{assetId}/withdraw")
    public String withdraw(@PathVariable Long assetId,
                           @RequestParam String amount,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes ra) {
        try {
            transactionService.withdraw(assetId, parseAmount(amount), currentUser(principal));
            ra.addFlashAttribute("success", "Withdrawal complete. Your balance has been updated.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/assets/" + assetId;
    }
}
