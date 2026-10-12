package com.crypton.repository;

import com.crypton.model.Asset;
import com.crypton.model.CoinType;
import com.crypton.model.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByVault(Vault vault);

    List<Asset> findByVaultVaultId(Long vaultId);

    Optional<Asset> findByVaultAndCoinType(Vault vault, CoinType coinType);

    Optional<Asset> findByWalletAddress(String walletAddress);
}
