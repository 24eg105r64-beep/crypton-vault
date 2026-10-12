package com.crypton.controller.api;

import com.crypton.dto.api.ApiDtos.AmountRequest;
import com.crypton.dto.api.ApiDtos.AssetDetailView;
import com.crypton.dto.api.ApiDtos.AssetView;
import com.crypton.dto.api.ApiDtos.TransactionView;
import com.crypton.dto.api.ApiDtos.VaultView;
import com.crypton.exception.InvalidAmountException;
import com.crypton.model.Asset;
import com.crypton.model.User;
import com.crypton.security.CustomUserDetails;
import com.crypton.service.AssetService;
import com.crypton.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * One coin: its balance, history, deposits and withdrawals.
 * Amount rules and balance checks live in the existing TransactionService.
 *
 * Deposit/withdraw are intentionally NOT wrapped in an extra transaction here,
 * so a failed withdrawal is still recorded as FAILED (as on the original page).
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class ApiAssetController {

    private final AssetService assetService;
    private final TransactionService transactionService;

    @GetMapping("/{assetId}")
    @Transactional(readOnly = true)
    public AssetDetailView detail(@PathVariable Long assetId,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        Asset asset = assetService.getAssetById(assetId, user);
        List<TransactionView> history = transactionService.getAssetTransactionHistory(assetId, user)
                .stream().map(TransactionView::of).toList();
        return new AssetDetailView(AssetView.of(asset), VaultView.of(asset.getVault()), history);
    }

    @PostMapping("/{assetId}/deposit")
    public TransactionView deposit(@PathVariable Long assetId,
                                   @RequestBody AmountRequest body,
                                   @AuthenticationPrincipal CustomUserDetails principal) {
        return TransactionView.of(transactionService.deposit(assetId, parseAmount(body), principal.getUser()));
    }

    @PostMapping("/{assetId}/withdraw")
    public TransactionView withdraw(@PathVariable Long assetId,
                                    @RequestBody AmountRequest body,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        return TransactionView.of(transactionService.withdraw(assetId, parseAmount(body), principal.getUser()));
    }

    private BigDecimal parseAmount(AmountRequest body) {
        if (body == null || body.amount() == null || body.amount().isBlank()) {
            throw new InvalidAmountException("Please enter an amount, like 0.5");
        }
        try {
            return new BigDecimal(body.amount().trim());
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Please enter a valid amount, like 0.5");
        }
    }
}
