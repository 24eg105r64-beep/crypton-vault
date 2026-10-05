package com.crypton.controller;

import com.crypton.model.Asset;
import com.crypton.model.CoinType;
import com.crypton.model.User;
import com.crypton.model.Vault;
import com.crypton.service.AssetService;
import com.crypton.service.UserService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vaults")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;
    private final AssetService assetService;
    private final UserService userService;

    // Finds the signed-in user from their email
    private User currentUser(UserDetails principal) {
        return userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Signed-in user not found"));
    }

    // Page: list of all my vaults
    @GetMapping
    public String listVaults(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = currentUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("vaults", vaultService.getUserVaults(user));
        return "vaults";
    }

    // Action: create a new vault
    @PostMapping
    public String createVault(@RequestParam String vaultName,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes ra) {
        if (vaultName == null || vaultName.isBlank()) {
            ra.addFlashAttribute("error", "Please give your vault a name.");
            return "redirect:/vaults";
        }
        try {
            Vault vault = vaultService.createVault(vaultName.trim(), currentUser(principal));
            ra.addFlashAttribute("success", "Your vault \"" + vault.getVaultName() + "\" is ready.");
            return "redirect:/vaults/" + vault.getVaultId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/vaults";
        }
    }

    // Page: one vault and its coins
    @GetMapping("/{vaultId}")
    public String viewVault(@PathVariable Long vaultId,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        User user = currentUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("vault", vaultService.getVaultById(vaultId, user));
        model.addAttribute("assets", assetService.getVaultAssets(vaultId, user));
        model.addAttribute("coinTypes", CoinType.values());
        return "vault-detail";
    }

    // Action: rename a vault
    @PostMapping("/{vaultId}/rename")
    public String renameVault(@PathVariable Long vaultId,
                              @RequestParam String newName,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes ra) {
        try {
            vaultService.renameVault(vaultId, newName.trim(), currentUser(principal));
            ra.addFlashAttribute("success", "Vault renamed.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vaults/" + vaultId;
    }

    // Action: delete a vault
    @PostMapping("/{vaultId}/delete")
    public String deleteVault(@PathVariable Long vaultId,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes ra) {
        try {
            vaultService.deleteVault(vaultId, currentUser(principal));
            ra.addFlashAttribute("success", "Vault deleted.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vaults";
    }

    // Action: add a coin to a vault
    @PostMapping("/{vaultId}/assets")
    public String addAsset(@PathVariable Long vaultId,
                           @RequestParam CoinType coinType,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes ra) {
        try {
            Asset asset = assetService.addAsset(vaultId, coinType, currentUser(principal));
            ra.addFlashAttribute("success", asset.getCoinType() + " added to your vault.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vaults/" + vaultId;
    }
}
