package com.crypton.service;

import com.crypton.model.Asset;
import com.crypton.model.CoinType;
import com.crypton.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AssetService {

    Asset addAsset(Long vaultId, CoinType coinType, User user);

    List<Asset> getVaultAssets(Long vaultId, User user);

    Asset getAssetById(Long assetId, User user);

    Map<CoinType, BigDecimal> getTotalBalancesByCoinType(User user);
}
