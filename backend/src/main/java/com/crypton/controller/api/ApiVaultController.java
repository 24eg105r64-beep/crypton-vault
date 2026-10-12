package com.crypton.controller.api;

import com.crypton.dto.api.ApiDtos.AddAssetRequest;
import com.crypton.dto.api.ApiDtos.AssetView;
import com.crypton.dto.api.ApiDtos.NameRequest;
import com.crypton.dto.api.ApiDtos.VaultDetailView;
import com.crypton.dto.api.ApiDtos.VaultView;
import com.crypton.model.CoinType;
import com.crypton.model.User;
import com.crypton.security.CustomUserDetails;
import com.crypton.service.AssetService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Vaults and the coins inside them. Ownership checks happen in the existing
 * VaultService / AssetService, exactly as for the original pages.
 */
@RestController
@RequestMapping("/api/vaults")
@RequiredArgsConstructor
public class ApiVaultController {

    private final VaultService vaultService;
    private final AssetService assetService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<VaultView> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return vaultService.getUserVaults(principal.getUser()).stream().map(VaultView::of).toList();
    }

    @PostMapping
    public ResponseEntity<VaultView> create(@RequestBody NameRequest body,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        String name = body == null || body.name() == null ? "" : body.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Please give your vault a name.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Vault names can be at most 100 characters.");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VaultView.of(vaultService.createVault(name, principal.getUser())));
    }

    @GetMapping("/{vaultId}")
    @Transactional(readOnly = true)
    public VaultDetailView detail(@PathVariable Long vaultId,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        VaultView vault = VaultView.of(vaultService.getVaultById(vaultId, user));
        List<AssetView> assets = assetService.getVaultAssets(vaultId, user).stream().map(AssetView::of).toList();
        List<String> coinTypes = Arrays.stream(CoinType.values()).map(Enum::name).toList();
        return new VaultDetailView(vault, assets, coinTypes);
    }

    @PutMapping("/{vaultId}")
    public VaultView rename(@PathVariable Long vaultId,
                            @RequestBody NameRequest body,
                            @AuthenticationPrincipal CustomUserDetails principal) {
        String name = body == null || body.name() == null ? "" : body.name().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Please give your vault a name.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Vault names can be at most 100 characters.");
        }
        return VaultView.of(vaultService.renameVault(vaultId, name, principal.getUser()));
    }

    @DeleteMapping("/{vaultId}")
    public ResponseEntity<Void> delete(@PathVariable Long vaultId,
                                       @AuthenticationPrincipal CustomUserDetails principal) {
        vaultService.deleteVault(vaultId, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{vaultId}/assets")
    public ResponseEntity<AssetView> addAsset(@PathVariable Long vaultId,
                                              @RequestBody AddAssetRequest body,
                                              @AuthenticationPrincipal CustomUserDetails principal) {
        CoinType coinType;
        try {
            coinType = CoinType.valueOf(body == null || body.coinType() == null ? "" : body.coinType().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Please choose one of the supported coins.");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AssetView.of(assetService.addAsset(vaultId, coinType, principal.getUser())));
    }
}
