package com.crypton.controller.api;

import com.crypton.dto.api.ApiDtos;
import com.crypton.dto.api.ApiDtos.ActivityView;
import com.crypton.dto.api.ApiDtos.DashboardView;
import com.crypton.dto.api.ApiDtos.UserView;
import com.crypton.dto.api.ApiDtos.VaultView;
import com.crypton.model.CoinType;
import com.crypton.model.User;
import com.crypton.security.CustomUserDetails;
import com.crypton.service.AssetService;
import com.crypton.service.AuditService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Everything the dashboard shows, in one request. */
@RestController
@RequiredArgsConstructor
public class ApiDashboardController {

    private final VaultService vaultService;
    private final AssetService assetService;
    private final AuditService auditService;

    @GetMapping("/api/dashboard")
    @Transactional(readOnly = true)
    public DashboardView dashboard(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();

        Map<String, String> totals = new LinkedHashMap<>();
        for (Map.Entry<CoinType, BigDecimal> entry : assetService.getTotalBalancesByCoinType(user).entrySet()) {
            totals.put(entry.getKey().name(), ApiDtos.plain(entry.getValue()));
        }

        return new DashboardView(
                UserView.of(user),
                totals,
                vaultService.getUserVaults(user).stream().map(VaultView::of).toList(),
                auditService.getUserAuditLogs(user).stream().limit(5).map(ActivityView::of).toList()
        );
    }
}
