package com.crypton.service;

import com.crypton.model.User;
import com.crypton.model.Vault;

import java.util.List;

public interface VaultService {

    Vault createVault(String vaultName, User user);

    List<Vault> getUserVaults(User user);

    Vault getVaultById(Long vaultId, User user);

    Vault renameVault(Long vaultId, String newName, User user);

    void deleteVault(Long vaultId, User user);
}
