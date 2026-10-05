package com.crypton.controller;

import com.crypton.model.User;
import com.crypton.service.AssetService;
import com.crypton.service.AuditService;
import com.crypton.service.UserService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final VaultService vaultService;
    private final AssetService assetService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Signed-in user not found"));

        model.addAttribute("user", user);
        model.addAttribute("vaults", vaultService.getUserVaults(user));
        model.addAttribute("totals", assetService.getTotalBalancesByCoinType(user));
        model.addAttribute("recentActivity",
                auditService.getUserAuditLogs(user).stream().limit(5).toList());
        return "dashboard";
    }
}
