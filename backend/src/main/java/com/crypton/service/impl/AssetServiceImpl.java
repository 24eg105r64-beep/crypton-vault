package com.crypton.service.impl;

import com.crypton.exception.AccessDeniedException;
import com.crypton.exception.ResourceNotFoundException;
import com.crypton.model.Asset;
import com.crypton.model.CoinType;
import com.crypton.model.User;
import com.crypton.model.Vault;
import com.crypton.repository.AssetRepository;
import com.crypton.repository.VaultRepository;
import com.crypton.service.AssetService;
import com.crypton.service.AuditService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final VaultRepository vaultRepository;
    private final VaultService vaultService;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public Asset addAsset(Long vaultId, CoinType coinType, User user) {
        if (coinType == null) {
            throw new IllegalArgumentException("Coin type cannot be null");
        }

        // Validate vault ownership
        Vault vault = vaultService.getVaultById(vaultId, user);

        // Generate placeholder wallet address: "0x" + 40 random hex characters
        byte[] addressBytes = new byte[20];
        secureRandom.nextBytes(addressBytes);
        StringBuilder sb = new StringBuilder("0x");
        for (byte b : addressBytes) {
            sb.append(String.format("%02x", b));
        }
        String walletAddress = sb.toString();

        Asset asset = Asset.builder()
                .vault(vault)
                .coinType(coinType)
                .balance(BigDecimal.ZERO)
                .walletAddress(walletAddress)
                .build();

        Asset saved = assetRepository.save(asset);
        auditService.logAction(user, "ASSET_ADDED");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asset> getVaultAssets(Long vaultId, User user) {
        Vault vault = vaultService.getVaultById(vaultId, user);
        return assetRepository.findByVault(vault);
    }

    @Override
    @Transactional(readOnly = true)
    public Asset getAssetById(Long assetId, User user) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + assetId));

        if (user == null || asset.getVault() == null || asset.getVault().getUser() == null ||
                !asset.getVault().getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("Access denied: You do not own asset with id " + assetId);
        }

        return asset;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<CoinType, BigDecimal> getTotalBalancesByCoinType(User user) {
        if (user == null) {
            throw new AccessDeniedException("User must be authenticated");
        }

        Map<CoinType, BigDecimal> totals = new EnumMap<>(CoinType.class);
        for (CoinType ct : CoinType.values()) {
            totals.put(ct, BigDecimal.ZERO);
        }

        List<Vault> userVaults = vaultRepository.findByUser(user);
        for (Vault vault : userVaults) {
            List<Asset> assets = assetRepository.findByVault(vault);
            for (Asset asset : assets) {
                totals.put(asset.getCoinType(), totals.get(asset.getCoinType()).add(asset.getBalance()));
            }
        }

        return totals;
    }
}
